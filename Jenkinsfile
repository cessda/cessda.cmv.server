pipeline {

    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '20')
    }

    environment {
        productName = "cmv"
        componentName = "server"
        IMAGE_TAG = "${docker_repo}/${productName}-${componentName}:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    agent any

    stages {
        // Building on master
        stage('Pull SDK Docker Image') {
            agent {
                docker {
                    image 'maven:3-jdk-11'
                    reuseNode true
                }
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh "$MVN_CMD clean install -Pdocker-compose"
                        }
                    }
                    when { branch 'master' }
                }
                // Not running on master - test only (for PRs and integration branches)
                stage('Test Project') {
                    steps {
                        withMaven {
                            sh "$MVN_CMD clean test -Pdocker-compose"
                        }
                    }
                    when { not { branch 'master' } }
                }
                stage('Record Issues') {
                    steps {
                        recordIssues aggregatingResults: true, tools: [java()]
                    }
                }
                stage('Run SonarQube Scan') {
                    steps {
                        withSonarQubeEnv('cessda-sonar') {
                            withMaven {
                                sh "$MVN_CMD sonar:sonar -Pdocker-compose"
                            }
                        }
                        timeout(time: 1, unit: 'HOURS') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                    when { branch 'master' }
                }
            }
        }
        stage('Build and Push Docker Image') {
            steps {
                sh 'gcloud auth configure-docker'
                withMaven(maven: 'maven-3-6') {
                    sh "mvn docker:build docker:push -Pdocker-compose -D\"docker.registry.host\"=${docker_repo} -D\"docker.image.name\"=${productName}-${componentName} -D\"docker.image.tag\"=${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
                }
                sh "gcloud container images add-tag ${IMAGE_TAG} ${docker_repo}/${productName}-${componentName}:${env.BRANCH_NAME}-latest"
            }
            when { branch 'master' }
        }
    }
}