pipeline {
  agent any
  triggers { pollSCM('H/2 * * * *') }
  environment {
    DOCKERHUB_USER = 'shashidhark'
    IMAGE_NAME = "${DOCKERHUB_USER}/calculator-api"
    IMAGE_TAG = "${BUILD_NUMBER}"
    COMPOSE_PROJECT_NAME = 'calculator'
  }
  stages {
    stage('Checkout') { steps { git branch: 'main', url: 'https://github.com/shashidharsai/calculator-devops.git'; sh 'git log -1 --oneline' } }
    stage('Build') { steps { sh 'mvn -B clean package -DskipTests' } }
    stage('Test') {
      steps { sh 'mvn -B test' }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }
    stage('Docker Build') {
      steps { sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .' }
    }
    stage('Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_LOGIN', passwordVariable: 'DH_PASS')]) {
          sh 'echo "$DH_PASS" | docker login -u "$DH_LOGIN" --password-stdin'
          sh 'docker push $IMAGE_NAME:$IMAGE_TAG && docker push $IMAGE_NAME:latest'
        }
      }
    }
    stage('Deploy') {
      steps {
        sh '''
          docker compose up -d --remove-orphans
          for i in $(seq 1 30); do
            if curl -sf http://localhost:8081/actuator/health | grep -q UP; then echo "Healthy"; exit 0; fi
            sleep 2
          done
          echo "Health check failed"; docker compose ps; exit 1
        '''
      }
    }
  }
  post {
    success {
      script {
        try { mail to: 'katakamshashidharsai@gmail.com', subject: "SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}", body: "Build ${BUILD_NUMBER} passed and was deployed.\n${BUILD_URL}" }
        catch (e) { echo "Email failed, build still OK: ${e}" }
      }
    }
    failure {
      script {
        try { mail to: 'katakamshashidharsai@gmail.com', subject: "FAILED: ${JOB_NAME} #${BUILD_NUMBER}", body: "Build ${BUILD_NUMBER} failed.\n${BUILD_URL}console" }
        catch (e) { echo "Email failed: ${e}" }
      }
    }
  }
}
