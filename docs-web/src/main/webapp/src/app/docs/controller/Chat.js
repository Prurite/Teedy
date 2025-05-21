/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *  I want to implement a new feature of real-time chat in the webapp, e.g., one-to-one chat with other online users from the same group.
 *  In the backend, the messages can be simple saved as sender, receiver, text, and other related necessary things.
 *  In the frontend, their is a floating chat button in the lower right corner, and when clicked, it opens a simple modal.
 *  The left is a list of past conversations, with a input bar to create a new conversation to some user; the right is chat history & text field.
 *  With reference to the attached context, implement the frontend part of the chat function.
 *  [ChatResource.java] [en.json] [index.html] [main.html] [document.html] [app.js] [Main.js] [Document.js]
 */

'use strict';

/**
 * Chat controller.
 */
angular.module('docs').controller('Chat', function ($scope, $rootScope, $interval, Restangular, $state, $uibModal) {
  $scope.open = false;
  $scope.conversations = [];
  $scope.activeConversation = null;
  $scope.messages = [];
  $scope.unreadCount = 0;
  $scope.messageContent = '';
  $scope.loading = false;
  $scope.searchQuery = '';
  $scope.users = [];
  $scope.pollingPromise = null;

  // Current pagination for messages
  $scope.messagesLimit = 50;
  $scope.messagesOffset = 0;
  $scope.messagesTotal = 0;
  $scope.loadingMore = false;

  /**
   * Initialize chat.
   */
  $scope.init = function () {
    // Load unread count
    $scope.loadUnreadCount();
    
    // Start polling for updates
    $scope.startPolling();
  };

  /**
   * Start polling for updates.
   */
  $scope.startPolling = function () {
    // Poll for unread messages every 10 seconds
    if ($scope.pollingPromise === null) {
      $scope.pollingPromise = $interval(function() {
        $scope.loadUnreadCount();
        
        // If chat is open and there's an active conversation, refresh messages
        if ($scope.open && $scope.activeConversation) {
          $scope.loadMessages($scope.activeConversation.userId, true); // Added true to reset messages
        }
        
        // Refresh conversations list if chat is open
        if ($scope.open) {
          $scope.loadConversations();
        }
      }, 10000); // Poll every 10 seconds
    }
  };

  /**
   * Stop polling.
   */
  $scope.stopPolling = function () {
    if ($scope.pollingPromise !== null) {
      $interval.cancel($scope.pollingPromise);
      $scope.pollingPromise = null;
    }
  };

  /**
   * Clean up when the controller is destroyed.
   */
  $scope.$on('$destroy', function() {
    $scope.stopPolling();
  });

  /**
   * Toggle the chat modal.
   */
  $scope.toggleChat = function () {
    $scope.open = !$scope.open;
    
    if ($scope.open) {
      // Load conversations when opening
      $scope.loadConversations();
      
      // Reset the unread count when opening
      $scope.unreadCount = 0;
    }
  };

  /**
   * Load all conversations.
   */
  $scope.loadConversations = function () {
    Restangular.one('chat/conversation').get().then(function (data) {
      $scope.conversations = data.conversations;
      
      // If we have an active conversation, update it with the latest data
      if ($scope.activeConversation) {
        const updated = $scope.conversations.find(c => c.userId === $scope.activeConversation.userId);
        if (updated) {
          $scope.activeConversation = updated;
        }
      }
    });
  };

  /**
   * Load messages for a specific conversation.
   */
  $scope.loadMessages = function (userId, reset = false) {
    if (reset) {
      $scope.messagesOffset = 0;
    }
    
    $scope.loading = true;
    
    Restangular.one('chat', userId)
      .get({
        limit: $scope.messagesLimit,
        offset: $scope.messagesOffset
      })
      .then(function (data) {
        if (reset) {
          // Reset messages completely when refreshing
          $scope.messages = data.messages;
        } else {
          // Only append when loading more (scrolling up in history)
          $scope.messages = $scope.messages.concat(data.messages);
        }
        
        $scope.messagesTotal = data.total;
        $scope.loading = false;
      });
  };

  /**
   * Load more messages for the current conversation.
   */
  $scope.loadMoreMessages = function () {
    if ($scope.loadingMore || $scope.messages.length >= $scope.messagesTotal) {
      return;
    }
    
    $scope.loadingMore = true;
    $scope.messagesOffset += $scope.messagesLimit;
    
    Restangular.one('chat', $scope.activeConversation.userId)
      .get({
        limit: $scope.messagesLimit,
        offset: $scope.messagesOffset
      })
      .then(function (data) {
        $scope.messages = $scope.messages.concat(data.messages);
        $scope.loadingMore = false;
      });
  };

  /**
   * Open a conversation.
   */
  $scope.openConversation = function (conversation) {
    $scope.activeConversation = conversation;
    $scope.messages = [];
    $scope.messagesOffset = 0;
    $scope.loadMessages(conversation.userId, true);
    
    // Mark all messages from this user as read
    Restangular.one('chat/conversation/' + conversation.userId + '/read').post().then(function () {
      // Update unread count after marking messages as read
      $scope.loadUnreadCount();
      
      // Update the conversation's unread count
      conversation.unreadCount = 0;
    });
  };

  /**
   * Send a message.
   */
  $scope.sendMessage = function () {
    console.log('Sending message:', $scope.messageContent);

    if (!$scope.messageContent || !$scope.activeConversation) {
      return;
    }
    
    const content = $scope.messageContent;
    $scope.messageContent = '';
    
    Restangular.one('chat').put({
      receiverId: $scope.activeConversation.userId,
      content: content
    }).then(function () {
      // Reload messages to show the new one
      $scope.loadMessages($scope.activeConversation.userId, true);
    });
  };

  /**
   * Load unread message count.
   */
  $scope.loadUnreadCount = function () {
    Restangular.one('chat/unread').get().then(function (data) {
      $scope.unreadCount = data.unread;
    });
  };

  /**
   * Open new conversation dialog.
   */
  $scope.openNewConversation = function () {
    // Load users to chat with
    Restangular.one('chat/user').get().then(function (data) {
      $scope.users = data.users;
      
      $uibModal.open({
        templateUrl: 'partial/docs/chat.new.html',
        controller: 'ChatNewConversation',
        resolve: {
          users: function () {
            return $scope.users;
          }
        }
      }).result.then(function (selectedUser) {
        if (selectedUser) {
          // Create a conversation object and open it
          const newConversation = {
            userId: selectedUser.id,
            username: selectedUser.username,
            unreadCount: 0
          };
          
          $scope.openConversation(newConversation);
        }
      });
    });
  };

  // Initialize chat
  $scope.init();
});

/**
 * Chat new conversation controller.
 */
angular.module('docs').controller('ChatNewConversation', function ($scope, $uibModalInstance, users) {
  $scope.users = users;
  $scope.selectedUser = null;
  $scope.searchText = '';
  
  $scope.filteredUsers = function() {
    if (!$scope.searchText) {
      return $scope.users;
    }
    
    return $scope.users.filter(function(user) {
      return user.username.toLowerCase().includes($scope.searchText.toLowerCase());
    });
  };
  
  $scope.selectUser = function(user) {
    $scope.selectedUser = user;
    $uibModalInstance.close(user);
  };
  
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };
});