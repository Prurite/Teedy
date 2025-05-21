pipeline {
    agent any

    environment {
        // Jenkins credentials ID for DockerHub
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials')
        
        // DockerHub repository name
        DOCKER_IMAGE = 'prurite/teedy-app'
        
        // Tag image with Jenkins build number
        DOCKER_TAG = "${env.BUILD_NUMBER}"
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

        stage('Upload image') {
            steps {
                script {
                    // Push image to Docker Hub
                    docker.withRegistry('https://registry.hub.docker.com', 'DOCKER_HUB_CREDENTIALS') {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    // Stop and remove existing container if exists
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'
                    
                    // Run container
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                        .run('--name teedy-container-8081 -d -p 8081:8080')

                    // List running Teedy containers
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
