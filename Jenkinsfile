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
          script {
            try { mail to: 'YOUR_EMAIL', subject: "SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}", body: "Build ${BUILD_NUMBER} passed and was deployed.\n${BUILD_URL}" }
            catch (e) { echo "Email failed, build still OK: ${e}" }
          }
        }
        failure {
          script {
            try { mail to: 'YOUR_EMAIL', subject: "FAILED: ${JOB_NAME} #${BUILD_NUMBER}", body: "Build ${BUILD_NUMBER} failed.\n${BUILD_URL}console" }
            catch (e) { echo "Email failed: ${e}" }
          }
        }
      }
}
