#!/bin/bash

# Configuration
AWS_REGION="us-east-1"
CLUSTER_NAME="service-publication-fargate-cluster"
SERVICE_NAME="service-publication-fargate-service"
ECR_REPOSITORY="service-publication-app"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "Setting up ECS Fargate for Service Publication Platform..."

# 1. Create ECR repository
echo "Creating ECR repository..."
aws ecr create-repository --repository-name $ECR_REPOSITORY --region $AWS_REGION

# 2. Create ECS Fargate cluster
echo "Creating ECS Fargate cluster..."
aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $AWS_REGION

# 3. Create CloudWatch log group
echo "Creating CloudWatch log group..."
aws logs create-log-group --log-group-name "/ecs/service-publication-fargate" --region $AWS_REGION

# 4. Get default VPC and subnets
echo "Getting VPC and subnet information..."
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=is-default,Values=true" --query 'Vpcs[0].VpcId' --output text)
SUBNET_IDS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text)
SUBNET_ID=$(echo $SUBNET_IDS | cut -d' ' -f1)

# 5. Create security group
echo "Creating security group..."
SECURITY_GROUP_ID=$(aws ec2 create-security-group \
    --group-name service-publication-fargate-sg \
    --description "Security group for ECS Fargate service publication" \
    --vpc-id $VPC_ID \
    --query 'GroupId' --output text)

# 6. Add security group rules
echo "Adding security group rules..."
aws ec2 authorize-security-group-ingress \
    --group-id $SECURITY_GROUP_ID \
    --protocol tcp \
    --port 8080 \
    --cidr 0.0.0.0/0

# 7. Create IAM roles (if they don't exist)
echo "Creating IAM roles..."
aws iam create-role \
    --role-name ecsTaskExecutionRole \
    --assume-role-policy-document '{
      "Version": "2012-10-17",
      "Statement": [
        {
          "Effect": "Allow",
          "Principal": {
            "Service": "ecs-tasks.amazonaws.com"
          },
          "Action": "sts:AssumeRole"
        }
      ]
    }' 2>/dev/null || echo "ecsTaskExecutionRole already exists"

aws iam create-role \
    --role-name ecsTaskRole \
    --assume-role-policy-document '{
      "Version": "2012-10-17",
      "Statement": [
        {
          "Effect": "Allow",
          "Principal": {
            "Service": "ecs-tasks.amazonaws.com"
          },
          "Action": "sts:AssumeRole"
        }
      ]
    }' 2>/dev/null || echo "ecsTaskRole already exists"

# 8. Attach policies to roles
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

aws iam attach-role-policy \
    --role-name ecsTaskRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# 9. Register task definition
echo "Registering task definition..."
aws ecs register-task-definition \
    --cli-input-json file://task-definition.json \
    --region $AWS_REGION

# 10. Create ECS service
echo "Creating ECS Fargate service..."
aws ecs create-service \
    --cluster $CLUSTER_NAME \
    --service-name $SERVICE_NAME \
    --task-definition service-publication-fargate \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_ID],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
    --region $AWS_REGION

echo "ECS Fargate setup completed successfully!"
echo "ECR Repository: $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY"
echo "ECS Cluster: $CLUSTER_NAME"
echo "ECS Service: $SERVICE_NAME"
echo "Security Group: $SECURITY_GROUP_ID" 