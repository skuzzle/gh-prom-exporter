pipeline {
  agent {
    docker {
      image 'maven:3.8.3-openjdk-17'
      args '-v /home/jenkins/.m2:/var/maven/.m2 -v /home/jenkins/.gnupg:/.gnupg -v /var/run/docker.sock:/var/run/docker.sock:ro -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS=-Duser.home=/var/maven'
    }
  }
  options { 
    disableConcurrentBuilds() 
  }
  environment {
    COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_gh_prom_exporter')
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B clean verify'
      }
    }
    stage('Coverage') {
      steps {
        sh 'mvn -B jacoco:report jacoco:report-integration coveralls:report -DrepoToken=$COVERALLS_REPO_TOKEN'
      }
    }
  }
}
