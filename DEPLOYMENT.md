# Cloud Run Deployment

This generated service includes GitHub Actions CI/CD for Google Cloud Run.

## Generated Values

| Setting | Value |
| --- | --- |
| GCP project | `probestack-prod` |
| Region | `us-central1` |
| Service name | `probestack-compliance-service-api` |
| Service account | `github-actions-deployer@probestack-prod.iam.gserviceaccount.com` |
| Workload identity provider | `projects/861433193200/locations/global/workloadIdentityPools/github-pool/providers/github-provider` |
| Artifact Registry host | `us-central1-docker.pkg.dev` |
| Artifact Registry repository | `probestack-prod/probestack-prod-apps` |

## Required GCP Setup

Before the workflow can deploy, confirm these resources exist:

1. A Google Cloud project with Cloud Run, Artifact Registry, IAM Credentials, and Cloud Build APIs enabled.
2. An Artifact Registry Docker repository matching the generated workflow values.
3. A deployer service account with permissions to push images and deploy Cloud Run services.
4. A Workload Identity Federation provider that trusts this GitHub repository.

## Local Docker Build

```bash
docker build -t probestack-compliance-service-api:local .
docker run --rm -p 8080:8080 probestack-compliance-service-api:local
```

## Manual Cloud Run Deploy

```bash
IMAGE=us-central1-docker.pkg.dev/probestack-prod/probestack-prod-apps/probestack-compliance-service-api:manual

docker build -t ${IMAGE} .
docker push ${IMAGE}

gcloud run deploy probestack-compliance-service-api \
  --image=${IMAGE} \
  --platform=managed \
  --region=us-central1 \
  --allow-unauthenticated \
  --port=8080
```

## GitHub Actions

The workflow at `.github/workflows/ci-cd.yml` runs on every push and deploys the latest commit to Cloud Run. Pull requests run build and test only.
