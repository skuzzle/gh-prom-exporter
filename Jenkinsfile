pipeline {
  options { 
    disableConcurrentBuilds() 
  }
  agent {
    docker {
      image 'maven:3.8.3-openjdk-17'
      args '-v /home/jenkins/.m2:/var/maven/.m2 -v /home/jenkins/.gnupg:/.gnupg -v /var/run/docker.sock:/var/run/docker.sock:ro -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS=-Duser.home=/var/maven'
    }
  }
  environment {
    DOCKER_REGISTRY = credentials('github_docker_registry')
    DOCKER_HOST = 'unix:///var/run/docker.sock'
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvnw -B clean verify'
      }
    }
    stage('Dockerize') {
      steps {
        sh 'mvnw -B "-Ddocker.publish.token=${DOCKER_REGISTRY_PSW}" -Dspring-boot.build-image.publish=true spring-boot:build-image'
      }
    }
  }
}
