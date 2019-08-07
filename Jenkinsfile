node('python') {  
    stage('pre-commit') {
            sh '''
               curl https://pre-commit.com/install-local.py | python -
               pre-commit
           '''
    }
}
buildPlugin()
