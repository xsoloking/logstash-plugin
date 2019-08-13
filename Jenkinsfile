node('linux') {
    stage('pre-commit') {
            infra.checkout(null)
            sh '''#!/bin/sh -eux
               curl https://pre-commit.com/install-local.py | python -
               "$HOME/bin/pre-commit" run --all-files
           '''
    }
}
buildPlugin()
