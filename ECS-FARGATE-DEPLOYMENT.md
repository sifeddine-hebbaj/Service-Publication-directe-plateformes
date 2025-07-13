# Guide de Déploiement ECS Fargate - Service Publication Platform

## Vue d'ensemble
Déploiement de l'application Spring Boot sur AWS ECS Fargate avec GitLab CI/CD.

## Architecture ECS Fargate
- **GitLab CI/CD** : Pipeline d'intégration continue
- **AWS ECR** : Registry Docker pour les images
- **AWS ECS Fargate** : Service de conteneurs serverless
- **AWS CloudWatch** : Logs et monitoring

## Prérequis

### 1. AWS CLI
```bash
# Installation
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configuration
aws configure
```

### 2. Permissions IAM
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecs:CreateCluster",
                "ecs:CreateService",
                "ecs:UpdateService",
                "ecs:RegisterTaskDefinition",
                "ecs:DescribeServices",
                "ecs:DescribeTaskDefinition",
                "ecr:CreateRepository",
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload",
                "ecr:PutImage",
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "iam:CreateRole",
                "iam:AttachRolePolicy"
            ],
            "Resource": "*"
        }
    ]
}
```

## Étapes de Déploiement

### Étape 1 : Préparation AWS ECS Fargate
```bash
# Rendre le script exécutable
chmod +x deploy-ecs-fargate.sh

# Exécuter le script de déploiement
./deploy-ecs-fargate.sh
```

### Étape 2 : Configuration GitLab Variables
Dans GitLab : **Settings > CI/CD > Variables**

| Variable | Description | Exemple |
|----------|-------------|---------|
| `AWS_ACCESS_KEY_ID` | Clé d'accès AWS | AKIA... |
| `AWS_SECRET_ACCESS_KEY` | Clé secrète AWS | wJalrXUtnFEMI... |
| `AWS_DEFAULT_REGION` | Région AWS | us-east-1 |
| `ECR_REPOSITORY` | URL ECR | 123456789.dkr.ecr.us-east-1.amazonaws.com/service-publication-app |
| `ECS_CLUSTER_NAME` | Nom du cluster ECS | service-publication-fargate-cluster |
| `ECS_SERVICE_NAME` | Nom du service ECS | service-publication-fargate-service |

### Étape 3 : Déploiement
1. Pousser le code vers la branche `main`
2. Le pipeline GitLab se déclenche automatiquement
3. Vérifier les logs dans GitLab CI/CD

### Étape 4 : Vérification
```bash
# Vérifier le service ECS Fargate
aws ecs describe-services \
    --cluster service-publication-fargate-cluster \
    --services service-publication-fargate-service

# Vérifier les tâches en cours
aws ecs list-tasks \
    --cluster service-publication-fargate-cluster

# Voir les logs CloudWatch
aws logs describe-log-groups \
    --log-group-name-prefix "/ecs/service-publication-fargate"
```

## Configuration ECS Fargate

### Task Definition
- **CPU** : 256 (0.25 vCPU)
- **Memory** : 512 MB
- **Network Mode** : awsvpc
- **Launch Type** : FARGATE

### Service Configuration
- **Desired Count** : 1
- **Health Check** : /actuator/health
- **Auto Scaling** : Désactivé par défaut

## Monitoring ECS Fargate

### CloudWatch Metrics
- CPU Utilization
- Memory Utilization
- Network I/O
- Application Logs

### Commandes de monitoring
```bash
# Voir les métriques CPU
aws cloudwatch get-metric-statistics \
    --namespace AWS/ECS \
    --metric-name CPUUtilization \
    --dimensions Name=ClusterName,Value=service-publication-fargate-cluster \
    --start-time $(date -d '1 hour ago' --iso-8601=seconds) \
    --end-time $(date --iso-8601=seconds) \
    --period 300 \
    --statistics Average

# Voir les logs d'une tâche
aws logs get-log-events \
    --log-group-name "/ecs/service-publication-fargate" \
    --log-stream-name "ecs/service-publication-container/container-id"
```

## Scaling ECS Fargate

### Auto Scaling
```bash
# Créer une politique d'auto-scaling
aws application-autoscaling register-scalable-target \
    --service-namespace ecs \
    --scalable-dimension ecs:service:DesiredCount \
    --resource-id service/service-publication-fargate-cluster/service-publication-fargate-service \
    --min-capacity 1 \
    --max-capacity 5

# Créer une politique de scaling basée sur CPU
aws application-autoscaling put-scaling-policy \
    --service-namespace ecs \
    --scalable-dimension ecs:service:DesiredCount \
    --resource-id service/service-publication-fargate-cluster/service-publication-fargate-service \
    --policy-name cpu-scaling-policy \
    --policy-type TargetTrackingScaling \
    --target-tracking-scaling-policy-configuration '{
        "TargetValue": 70.0,
        "PredefinedMetricSpecification": {
            "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
        }
    }'
```

## Troubleshooting ECS Fargate

### Problèmes courants

1. **Service ne démarre pas**
```bash
# Vérifier les logs de la tâche
aws ecs describe-tasks \
    --cluster service-publication-fargate-cluster \
    --tasks $(aws ecs list-tasks --cluster service-publication-fargate-cluster --query 'taskArns[0]' --output text)
```

2. **Image non trouvée**
```bash
# Vérifier l'image dans ECR
aws ecr describe-images \
    --repository-name service-publication-app
```

3. **Problèmes de réseau**
```bash
# Vérifier les security groups
aws ec2 describe-security-groups \
    --group-ids $(aws ecs describe-services --cluster service-publication-fargate-cluster --services service-publication-fargate-service --query 'services[0].networkConfiguration.awsvpcConfiguration.securityGroups[0]' --output text)
```

### Commandes utiles
```bash
# Redémarrer le service
aws ecs update-service \
    --cluster service-publication-fargate-cluster \
    --service service-publication-fargate-service \
    --force-new-deployment

# Mettre à jour la task definition
aws ecs register-task-definition \
    --cli-input-json file://task-definition.json

# Voir les événements du service
aws ecs describe-services \
    --cluster service-publication-fargate-cluster \
    --services service-publication-fargate-service \
    --query 'services[0].events'
```

## Coûts ECS Fargate

### Estimation mensuelle
- **CPU (256) + Memory (512MB)** : ~$15-25/mois
- **ECR Storage** : ~$2-5/mois
- **CloudWatch Logs** : ~$5-10/mois
- **Data Transfer** : ~$2-5/mois
- **Total estimé** : **$25-45/mois**

### Optimisation des coûts
1. Utiliser des instances spot (non disponible en Fargate)
2. Optimiser la taille des images Docker
3. Configurer la rétention des logs CloudWatch
4. Utiliser l'auto-scaling pour réduire les coûts

## Sécurité ECS Fargate

### Bonnes pratiques
1. Utiliser des IAM Roles avec permissions minimales
2. Chiffrer les données en transit et au repos
3. Mettre à jour régulièrement les images Docker
4. Monitorer les accès et logs
5. Utiliser des security groups restrictifs

### Configuration de sécurité
```bash
# Créer un security group plus restrictif
aws ec2 create-security-group \
    --group-name service-publication-fargate-restricted-sg \
    --description "Restricted security group for ECS Fargate" \
    --vpc-id $VPC_ID

# Autoriser seulement les IPs spécifiques
aws ec2 authorize-security-group-ingress \
    --group-id $SECURITY_GROUP_ID \
    --protocol tcp \
    --port 8080 \
    --cidr YOUR_IP_RANGE/32
```

## Support

Pour toute question :
1. Vérifier les logs GitLab CI/CD
2. Consulter les logs CloudWatch
3. Vérifier la documentation AWS ECS
4. Contacter l'équipe DevOps 