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
                    // Build Docker image
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }
        /*
        stage('Upload image') {
            steps {
                script {
                    // Push image to Docker Hub
                    docker.withRegistry('https://registry.hub.docker.com', "${env.DOCKER_HUB_CREDENTIALS}") {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }
        */
        stage('Start Minikube and Set up kubectl') {
            steps {
                script {
                    // Start Minikube if not already running
                    sh 'minikube start'

                    // Set the kubectl context to use Minikube
                    sh 'kubectl config use-context minikube'
                }
            }
        }
        stage('Deploy to Minikube') {
            steps {
                script {
                    // Create a Kubernetes deployment using the Docker image
                    sh "kubectl create deployment teedy-app --image=${env.DOCKER_IMAGE}:${env.DOCKER_TAG} || kubectl set image deployment/teedy-app teedy-app=${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"

                    // Expose the deployment as a service
                    sh 'kubectl expose deployment teedy-app --type=NodePort --name=teedy-service --port=8081 --target-port=8080'

                    // Get Minikube service URL
                    def serviceURL = sh(script: 'minikube service teedy-service --url', returnStdout: true).trim()

                    // Print the service URL
                    echo "Teedy app is available at: ${serviceURL}"
                }
            }
        }
    }
}
