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
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Building image') {
            steps {
                script {
                    // Configure Docker to use Minikube's Docker daemon
                    sh 'eval $(minikube docker-env)'
                    
                    // Build Docker image directly in Minikube's Docker daemon
                    sh "docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ."
                }
            }
        }
        stage('Deploy to Minikube') {
            steps {
                script {
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

                    // Get Minikube service URL
                    def serviceURL = sh(script: 'minikube service teedy-service --url', returnStdout: true).trim()

                    // Print the service URL
                    echo "Teedy app is available at: ${serviceURL}"
                }
            }
        }
    }
}
