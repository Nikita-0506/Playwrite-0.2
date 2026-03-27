pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'Java21'
    }

    triggers {
        pollSCM('*/5 * * * *')
    }

    parameters {
        choice(name: 'BROWSER', choices: ['Chrome', 'Firefox'], description: 'Select browser')
        choice(name: 'ENV', choices: ['QA', 'DEV', 'PROD'], description: 'Select environment')
    }

    environment {
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'qa-accelerator-inadev'
        S3_PREFIX = 'all_reports'
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '🔍 Checking out code from GitHub...'
                checkout scm
                script {
                    env.BUILD_TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
                }
            }
        }

        stage('Install Playwright') {
            steps {
                echo '🎭 Installing Playwright...'
                script {
                    if (isUnix()) {
                        sh 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
                    } else {
                        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
                    }
                }
            }
        }

        stage('Run Tests') {
            options {
                timeout(time: 60, unit: 'MINUTES')
                retry(1)
            }
            steps {
                echo "🧪 Running Tests on ${params.BROWSER} in ${params.ENV}"
                script {
                    // -Dmaven.test.failure.ignore=true ensures Maven always completes,
                    // writes all Surefire/Cucumber XML reports, and generates Extent Report
                    // even when tests fail — so downstream stages (S3 upload) always run.
                    def mvnCmd = "mvn clean verify -Dbrowser=${params.BROWSER} -Denv=${params.ENV} " +
                                 "-Dmaven.test.failure.ignore=true -Dsurefire.failIfNoSpecifiedTests=false"

                    def status
                    if (isUnix()) {
                        status = sh(script: mvnCmd, returnStatus: true)
                    } else {
                        status = bat(script: mvnCmd, returnStatus: true)
                    }

                    // Parse Surefire XML to get actual test counts
                    def xmlFiles = findFiles(glob: 'target/surefire-reports/TEST-*.xml')
                    int totalTests = 0, failedTests = 0, skippedTests = 0

                    for (def f : xmlFiles) {
                        def content = readFile(f.path)
                        def matcher = content =~ /tests="(\d+)"/
                        if (matcher) totalTests += matcher[0][1].toInteger()
                        matcher = content =~ /failures="(\d+)"/
                        if (matcher) failedTests += matcher[0][1].toInteger()
                        matcher = content =~ /errors="(\d+)"/
                        if (matcher) failedTests += matcher[0][1].toInteger()
                        matcher = content =~ /skipped="(\d+)"/
                        if (matcher) skippedTests += matcher[0][1].toInteger()
                    }

                    int passedTests = totalTests - failedTests - skippedTests

                    if (totalTests > 0) {
                        echo "📊 Test Results: ${passedTests} passed | ${failedTests} failed | ${skippedTests} skipped | ${totalTests} total"
                    }

                    if (status != 0 && totalTests == 0) {
                        // Maven failed before any tests ran → real build/compile error
                        currentBuild.result = 'FAILURE'
                        error("❌ Maven build failed — no tests were executed (possible compile error or plugin failure)")
                    } else if (failedTests > 0) {
                        // Tests ran but some failed → mark UNSTABLE so pipeline continues for report upload
                        unstable("⚠️ ${failedTests} test(s) failed out of ${totalTests} — reports will still be uploaded")
                    } else {
                        echo "✅ All ${passedTests} tests passed!"
                    }
                }
            }
        }

        // ✅ UPDATED S3 LOGIC (FROM CODE A)
        stage('Upload Reports to S3') {
            steps {
                echo '📤 Uploading reports to S3...'
                script {
                    try {
                        def shortCommit = isUnix()
                            ? sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()
                            : bat(script: '@git rev-parse --short=7 HEAD', returnStdout: true).trim().readLines().last()
                        def folderName = "run${BUILD_NUMBER}_${shortCommit}_${BUILD_TIMESTAMP}"
                        def s3Path = "s3://${S3_BUCKET}/${S3_PREFIX}/${folderName}"

                        echo "📂 Folder: ${folderName}"

                        if (isUnix()) {
                            sh """
                                # Upload Surefire Reports
                                if [ -d target/surefire-reports ]; then
                                    aws s3 cp target/surefire-reports ${s3Path}/surefire-reports/ --recursive --exclude "old/*" --region ${AWS_REGION}
                                fi

                                # Upload Cucumber Reports
                                if [ -d target/cucumber-reports ]; then
                                    aws s3 cp target/cucumber-reports ${s3Path}/cucumber-reports/ --recursive --region ${AWS_REGION}
                                fi

                                # Upload Latest Spark Report
                                if [ -d test-output ]; then
                                    LATEST=\$(find test-output -maxdepth 1 -type d -name "SparkReports*" | sort -r | head -1)
                                    if [ -n "\$LATEST" ]; then
                                        aws s3 cp "\$LATEST" ${s3Path}/extent-report/ --recursive --region ${AWS_REGION}
                                    fi
                                fi

                                # Upload Screenshots
                                if [ -d target/screenshots ]; then
                                    aws s3 cp target/screenshots ${s3Path}/screenshots/ --recursive --region ${AWS_REGION}
                                fi

                                # Upload Videos
                                if [ -d target/videos ]; then
                                    aws s3 cp target/videos ${s3Path}/videos/ --recursive --region ${AWS_REGION}
                                fi

                                # Upload JaCoCo Report
                                if [ -d target/site/jacoco ]; then
                                    aws s3 cp target/site/jacoco ${s3Path}/jacoco/ --recursive --region ${AWS_REGION}
                                fi

                                # Metadata
                                cat > build-metadata.json <<EOF
{
  "build_number": "${BUILD_NUMBER}",
  "commit_id": "${shortCommit}",
  "timestamp": "${BUILD_TIMESTAMP}",
  "status": "${currentBuild.result ?: 'SUCCESS'}",
  "browser": "${params.BROWSER}",
  "environment": "${params.ENV}",
  "job": "${JOB_NAME}",
  "url": "${BUILD_URL}"
}
EOF
                                aws s3 cp build-metadata.json ${s3Path}/build-metadata.json --region ${AWS_REGION}
                            """
                        } else {
                            def buildStatus = currentBuild.result ?: 'SUCCESS'
                            writeFile file: 'build-metadata.json', text: """{
  \"build_number\": \"${BUILD_NUMBER}\",
  \"commit_id\": \"${shortCommit}\",
  \"timestamp\": \"${BUILD_TIMESTAMP}\",
  \"status\": \"${buildStatus}\",
  \"browser\": \"${params.BROWSER}\",
  \"environment\": \"${params.ENV}\",
  \"job\": \"${JOB_NAME}\",
  \"url\": \"${BUILD_URL}\"
}"""
                            bat """
                                if exist target\\surefire-reports aws s3 cp target/surefire-reports ${s3Path}/surefire-reports/ --recursive --exclude "old/*" --region ${AWS_REGION}
                                if exist target\\cucumber-reports aws s3 cp target/cucumber-reports ${s3Path}/cucumber-reports/ --recursive --region ${AWS_REGION}
                                if exist target\\screenshots aws s3 cp target/screenshots ${s3Path}/screenshots/ --recursive --region ${AWS_REGION}
                                if exist target\\videos aws s3 cp target/videos ${s3Path}/videos/ --recursive --region ${AWS_REGION}
                                if exist target\\site\\jacoco aws s3 cp target/site/jacoco ${s3Path}/jacoco/ --recursive --region ${AWS_REGION}
                                aws s3 cp build-metadata.json ${s3Path}/build-metadata.json --region ${AWS_REGION}
                            """
                            powershell """
                                \$latest = Get-ChildItem -Path 'test-output' -Filter 'SparkReports*' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
                                if (\$latest) {
                                    aws s3 cp \$latest.FullName '${s3Path}/extent-report/' --recursive --region ${AWS_REGION}
                                }
                            """
                        }

                        def url = "https://${S3_BUCKET}.s3.${AWS_REGION}.amazonaws.com/${S3_PREFIX}/${folderName}"
                        echo "📊 Reports: ${url}"

                    } catch (Exception e) {
                        echo "⚠️ Upload failed: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo '🚀 Deploying application...'
            }
        }
    }

    post {
        always {
            echo '📊 Publishing Reports...'

            script {
                // Copy latest timestamped SparkReports folder to a fixed path for publishHTML
                if (isUnix()) {
                    sh '''
                        LATEST=$(find test-output -maxdepth 1 -type d -name "SparkReports*" 2>/dev/null | sort -r | head -1)
                        if [ -n "$LATEST" ]; then
                            rm -rf test-output/latest-report
                            cp -r "$LATEST" test-output/latest-report
                        fi
                    '''
                } else {
                    powershell '''
                        $latestDir = Get-ChildItem -Path "test-output" -Filter "SparkReports*" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
                        if ($latestDir) {
                            if (Test-Path "test-output\\latest-report") { Remove-Item "test-output\\latest-report" -Recurse -Force }
                            Copy-Item $latestDir.FullName "test-output\\latest-report" -Recurse -Force
                        }
                    '''
                }
            }

            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports',
                reportFiles: 'index.html',
                reportName: 'Cucumber Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Surefire Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'test-output/latest-report',
                reportFiles: 'Report/TestRunReport.html',
                reportName: 'Extent Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Report'
            ])

            archiveArtifacts artifacts: '**/target/**/*.*, **/test-output/**/*.*', allowEmptyArchive: true

            cleanWs()
        }

        success {
            echo '✅ Build SUCCESS'
        }

        failure {
            echo '❌ Build FAILED'
        }

        unstable {
            echo '⚠️ Build UNSTABLE'
        }
    }
}