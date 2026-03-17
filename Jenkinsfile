pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    triggers {
        // Poll GitHub every 15 minutes for changes
        pollSCM('H/15 * * * *')
        
        // Alternatively, use cron for scheduled builds regardless of changes:
        // cron('H 9,17 * * 1-5')  // Run at 9 AM and 5 PM on weekdays
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔍 Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Install Playwright') {
            steps {
                echo '🎭 Installing Playwright browsers...'
                bat '''
                    mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
                '''
            }
        }

        stage('Run Tests') {
            steps {
                echo '🧪 Running Playwright tests...'
                script {
                    // Capture the exit code
                    def testResult = bat(script: 'mvn clean test', returnStatus: true)
                    
                    // Explicitly fail if tests failed
                    if (testResult != 0) {
                        currentBuild.result = 'FAILURE'
                        error("❌ Tests failed! Stopping pipeline.")
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo '🚀 Deploying application...'
                // Your deployment steps here
            }
        }
    }

    post {
        always {
            echo '📊 Publishing test results and reports...'

            // Publish JUnit XML test results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            // Publish Cucumber HTML Report (if it exists)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports',
                reportFiles: 'index.html',
                reportName: 'Cucumber HTML Report',
                reportTitle: 'Test Report'
            ])

            // Publish Surefire HTML Report
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Surefire HTML Report',
                reportTitle: 'Surefire Report'
            ])

            // Archive all reports as artifacts
            archiveArtifacts artifacts: '**/target/cucumber-reports/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/surefire-reports/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/test-output/**/*.*', allowEmptyArchive: true
        }
        success {
            echo '✅ Tests passed successfully!'
        }
        failure {
            echo '❌ Tests failed! Pipeline stopped.'
        }
    }
}