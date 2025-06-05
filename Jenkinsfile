pipeline {
    agent any
    environment {
        // Jenkins credentials ID for DockerHub
        DOCKER_HUB_CREDENTIALS = '642daa11-5b6a-453e-82d2-83e0c46e9be4'
        
        // DockerHub repository name
        DOCKER_IMAGE = 'prurite/teedy-app'
        
        // Tag image with Jenkins build number
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        
        // Minikube environment variables
        MINIKUBE_HOME = '/tmp/.minikube'  // Ensure Minikube path is available
    }
    stages {
        stage('Setup Minikube') {
            steps {
                script {
                    // Start Minikube if not running
                    sh '''
                        if ! minikube status | grep -q "apiserver: Running"; then
                            echo "Starting Minikube..."
                            minikube start --driver=docker
                        else
                            echo "Minikube is already running"
                        fi
                    '''
                }
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Building image') {
            steps {
                script {
                    // Configure Docker to use Minikube's Docker daemon and build image
                    sh '''
                        eval $(minikube docker-env)
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    '''
                }
            }
        }
        stage('Deploy to Minikube') {
            steps {
                script {
                    // Ensure Minikube is running before deployment
                    sh 'minikube status'
                    
                    // Create deployment with imagePullPolicy: Never to use local image
                    sh """
                        kubectl create deployment teedy-app --image=${env.DOCKER_IMAGE}:${env.DOCKER_TAG} --dry-run=client -o yaml | \\
                        sed 's/imagePullPolicy: Always/imagePullPolicy: Never/' | \\
                        kubectl apply -f -
                    """
                    
                    // Alternative: Update existing deployment if it exists
                    sh "kubectl set image deployment/teedy-app teedy-app=${env.DOCKER_IMAGE}:${env.DOCKER_TAG} || true"
                    
                    // Set imagePullPolicy to Never for existing deployment
                    sh "kubectl patch deployment teedy-app -p '{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"teedy-app\",\"imagePullPolicy\":\"Never\"}]}}}}'"

                    // Expose the deployment as a service (only if it doesn't exist)
                    sh 'kubectl expose deployment teedy-app --type=NodePort --name=teedy-service --port=8081 --target-port=8080 || true'

                    // Wait for deployment to be ready
                    sh 'kubectl rollout status deployment/teedy-app --timeout=300s'

                    // Get Minikube service URL with error handling
                    script {
                        try {
                            def serviceURL = sh(script: 'minikube service teedy-service --url', returnStdout: true).trim()
                            echo "Teedy app is available at: ${serviceURL}"
                        } catch (Exception e) {
                            echo "Failed to get service URL: ${e.getMessage()}"
                            sh 'kubectl get services'
                            sh 'minikube ip'
                            sh 'kubectl get service teedy-service -o wide'
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            // Cleanup or status check
            sh 'kubectl get pods'
            sh 'kubectl get services'
        }
        failure {
            // Debug information on failure
            sh 'minikube status || true'
            sh 'kubectl describe deployment teedy-app || true'
        }
    }
}
