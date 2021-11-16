pipeline {
  agent any
  options { 
    disableConcurrentBuilds() 
  }
  parameters {
    string(name: 'VERSION', defaultValue: 'latest', description: 'The docker image version to deploy')
  }
  environment {
    DOCKER_REGISTRY = credentials('github_docker_registry')
  }
  stages {
    stage('Build Image') {
      agent {
        docker {
          image 'maven:3.8.3-openjdk-17'
          args '-u root -v /home/jenkins/.m2:/var/maven/.m2 -v /home/jenkins/.gnupg:/.gnupg -v /var/run/docker.sock:/var/run/docker.sock:ro -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS=-Duser.home=/var/maven'
        }
      }
      steps {
        sh 'mvn -B "-Ddocker.publish.usr=\${DOCKER_REGISTRY_USR}" "-Ddocker.publish.psw=\${DOCKER_REGISTRY_PSW}" -Dspring-boot.build-image.publish=true clean spring-boot:build-image'
      }
    }
    stage('Deploy') {
      agent any
      when {
        branch 'master'
      }
      steps {
        sh 'docker stack deploy --compose-file swarm-stack/production.yml --prune --with-registry-auth gh-prom-exporter'
      }
    }
  }
}
