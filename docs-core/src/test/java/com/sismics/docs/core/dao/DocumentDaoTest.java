package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentDaoTest {

    private DocumentDao documentDao;

    @BeforeEach
    public void setUp() {
        documentDao = new DocumentDao();
    }

    @Test
    public void testCreateDocument() {
        Document doc = new Document();
        String userId = "user123";

        EntityManager em = mock(EntityManager.class);
        ThreadLocalContext context = mock(ThreadLocalContext.class);

        try (MockedStatic<ThreadLocalContext> contextMock = Mockito.mockStatic(ThreadLocalContext.class);
             MockedStatic<AuditLogUtil> auditLogMock = Mockito.mockStatic(AuditLogUtil.class)) {

            contextMock.when(ThreadLocalContext::get).thenReturn(context);
            when(context.getEntityManager()).thenReturn(em);

            String id = documentDao.create(doc, userId);

            assertNotNull(id);
            assertEquals(id, doc.getId());
            assertNotNull(doc.getUpdateDate());

            verify(em).persist(doc);
            auditLogMock.verify(() -> AuditLogUtil.create(doc, AuditLogType.CREATE, userId));
        }
    }

    @Test
    public void testFindAllDocuments() {
        EntityManager em = mock(EntityManager.class);
        ThreadLocalContext context = mock(ThreadLocalContext.class);
        TypedQuery<Document> query = mock(TypedQuery.class);

        try (MockedStatic<ThreadLocalContext> contextMock = Mockito.mockStatic(ThreadLocalContext.class)) {
            contextMock.when(ThreadLocalContext::get).thenReturn(context);
            when(context.getEntityManager()).thenReturn(em);
            when(em.createQuery(anyString(), eq(Document.class))).thenReturn(query);
            when(query.setFirstResult(anyInt())).thenReturn(query);
            when(query.setMaxResults(anyInt())).thenReturn(query);
            when(query.getResultList()).thenReturn(List.of(new Document(), new Document()));

            List<Document> result = documentDao.findAll(0, 10);

            assertEquals(2, result.size());
        }
    }

    @Test
    public void testGetById_Found() {
        String docId = "abc123";
        Document doc = new Document();
        doc.setId(docId);

        EntityManager em = mock(EntityManager.class);
        ThreadLocalContext context = mock(ThreadLocalContext.class);
        TypedQuery<Document> query = mock(TypedQuery.class);

        try (MockedStatic<ThreadLocalContext> contextMock = Mockito.mockStatic(ThreadLocalContext.class)) {
            contextMock.when(ThreadLocalContext::get).thenReturn(context);
            when(context.getEntityManager()).thenReturn(em);
            when(em.createQuery(anyString(), eq(Document.class))).thenReturn(query);
            when(query.setParameter("id", docId)).thenReturn(query);
            when(query.getSingleResult()).thenReturn(doc);

            Document result = documentDao.getById(docId);

            assertNotNull(result);
            assertEquals(docId, result.getId());
        }
    }

    @Test
    public void testDelete() {
        String docId = "docToDelete";
        String userId = "adminUser";
        Document doc = new Document();
        doc.setId(docId);

        EntityManager em = mock(EntityManager.class);
        ThreadLocalContext context = mock(ThreadLocalContext.class);
        TypedQuery<Document> typedQuery = mock(TypedQuery.class);

        when(em.createQuery(startsWith("select d from Document"), eq(Document.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("id", docId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(doc);

        Query q1 = mock(Query.class);
        Query q2 = mock(Query.class);
        Query q3 = mock(Query.class);
        Query q4 = mock(Query.class);

        when(em.createQuery(startsWith("update File"))).thenReturn(q1);
        when(em.createQuery(startsWith("update Acl"))).thenReturn(q2);
        when(em.createQuery(startsWith("update DocumentTag"))).thenReturn(q3);
        when(em.createQuery(startsWith("update Relation"))).thenReturn(q4);

        for (Query q : List.of(q1, q2, q3, q4)) {
            when(q.setParameter(eq("documentId"), eq(docId))).thenReturn(q);
            when(q.setParameter(eq("dateNow"), any())).thenReturn(q);
            when(q.executeUpdate()).thenReturn(1);
        }

        try (MockedStatic<ThreadLocalContext> contextMock = Mockito.mockStatic(ThreadLocalContext.class);
             MockedStatic<AuditLogUtil> auditLogMock = Mockito.mockStatic(AuditLogUtil.class)) {

            contextMock.when(ThreadLocalContext::get).thenReturn(context);
            when(context.getEntityManager()).thenReturn(em);

            documentDao.delete(docId, userId);

            assertNotNull(doc.getDeleteDate());
            auditLogMock.verify(() -> AuditLogUtil.create(doc, AuditLogType.DELETE, userId));
        }
    }
}
