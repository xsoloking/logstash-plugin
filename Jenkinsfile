node('linux') {  
    stage('pre-commit') {
            checkout scm
            sh '''
               git --version
               curl https://pre-commit.com/install-local.py | python -
               /home/jenkins/bin/pre-commit run --all-files
           '''
    }
}
buildPlugin()
