#!/bin/bash
set -eo pipefail
aws cloudformation package --template-file template.yml --output-template-file packaged.yaml --s3-bucket equipment-api-lambda-deployment-artifacts
aws cloudformation deploy --template-file packaged.yml --stack-name equipment-api-stack --capabilities CAPABILITY_NAMED_IAM