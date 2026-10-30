pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Source code is checked out from GitHub.'
                sh 'ls -la'
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean test'
            }
        }

        stage('Package Application') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t calculator-devops:latest .'
            }
        }

        stage('Verify Docker Image') {
            steps {
                sh 'docker images calculator-devops:latest'
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: GitHub checkout, tests, packaging, and Docker build completed.'
        }

        failure {
            echo 'FAILED: Check the stage logs in Jenkins Console Output.'
        }
    }
}
