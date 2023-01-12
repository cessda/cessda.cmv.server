pipeline {

    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '20')
		disableConcurrentBuilds abortPrevious: true
    }

    environment {
        productName = "cmv"
        componentName = "server"
        IMAGE_TAG = "${docker_repo}/${productName}-${componentName}:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    agent {
        label 'jnlp-himem'
    }

    stages {
        // Building on master
        stage('Pull SDK Docker Image') {
            agent {
                docker {
                    image 'openjdk:17'
                    reuseNode true
                }
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean install -Pdocker-compose'
                        }
                    }
                    when { branch 'main' }
                }
                // Not running on master - test only (for PRs and integration branches)
                stage('Test Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean test -Pdocker-compose'
                        }
                    }
                    when { not { branch 'main' } }
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
                                sh './mvnw sonar:sonar -Pdocker-compose'
                            }
                        }
                        timeout(time: 1, unit: 'HOURS') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                    when { branch 'main' }
                }
            }
        }
        stage('Build and Push Docker Image') {
            steps {
                sh 'gcloud auth configure-docker'
                sh "./mvnw docker:build docker:push -Pdocker-compose -D\"docker.registry.host\"=${docker_repo} -D\"docker.image.name\"=${productName}-${componentName} -D\"docker.image.tag\"=${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
                sh "gcloud container images add-tag ${IMAGE_TAG} ${docker_repo}/${productName}-${componentName}:${env.BRANCH_NAME}-latest"
            }
            when { branch 'main' }
        }
        stage('Deploy CMV Server') {
            steps {
                build job: 'cessda.cmv.deploy/main', parameters: [string(name: 'serverImageTag', value: "${env.BRANCH_NAME}-${env.BUILD_NUMBER}")], wait: false
            }
            when { branch 'main' }
        }
    }
}
