# probestack-compliance-service-api

Generated Spring Boot application from OpenAPI specification.

## Project Details
- **Group ID**: com.probestack.forgesphere
- **Artifact ID**: probestack-compliance-service-api
- **Version**: 1.0.0
- **Base Package**: com.probestack.forgesphere

## Building the Project
```bash
mvn clean install
```


## Running the Application
```bash
mvn spring-boot:run
```

## API Documentation
Once the application is running, access the Swagger UI at:
- http://localhost:8080/swagger-ui.html

## API Docs (OpenAPI)
- http://localhost:8080/api-docs

## Compliance cut-offs (opt-in)

A cut-off is the share of rules an asset must pass before it counts as compliant. It is
**opt-in per (scan kind, asset type)** and there is no default row.

### If your team does not use cut-offs, nothing changes

The stored `compliance` verdict is still produced the same way it always has been — any failed
rule makes a scan `NON_COMPLIANT` — and a cut-off never rewrites it. Scan-history responses gain
no new fields unless an **enabled** cut-off exists for that scan's kind and asset type. With none
configured, responses are byte-for-byte what they were before. This is pinned by
`ComplianceThresholdServiceTest`; the `leavesSummaryUntouched…` and `doesNotLeakAcross…` tests
fail if it is ever broken.

### Configuring one

```
GET    /governance/v1/compliance-thresholds?assetType=APIGEE
GET    /governance/v1/compliance-thresholds/{scanKind}?assetType=APIGEE
PUT    /governance/v1/compliance-thresholds/{scanKind}
DELETE /governance/v1/compliance-thresholds/{scanKind}?assetType=APIGEE
```

`scanKind` is `COMPLIANCE`, `OWASP` or `LINTING` (case-insensitive). `PUT` body:

```json
{ "assetType": "APIGEE", "threshold": 80, "enabled": true, "updatedBy": "you@example.com" }
```

`threshold` is a percentage, 0-100 inclusive; anything outside is rejected with 400. `enabled`
defaults to `true` when omitted — send `false` to stage a cut-off without it taking effect.
`DELETE` restores the original verdict semantics for that pair.

### What appears on scan history once enabled

Three additive fields, alongside the untouched `compliance`:

| Field | Meaning |
| --- | --- |
| `passRate` | percentage of rules that passed, 0-100 |
| `threshold` | the cut-off in force |
| `complianceAtThreshold` | `COMPLIANT` when `passRate >= threshold`, else `NON_COMPLIANT` |

Notes on the semantics:

- **Skipped rules count against the pass rate.** A rule that could not be evaluated is not
  evidence of compliance, so it stays in the denominator.
- **The verdict is binary.** A cut-off is a single bar, so `PARTIAL` never comes from this path —
  it remains reachable only from the original verdict.
- **The verdict is computed at read time, not stored.** Raising or lowering a cut-off re-reads
  existing history rather than needing a migration or a re-scan.

## Asset exemptions (opt-in)

An exemption records that an asset's failures have been seen and consciously set aside — a
governance decision, made after the scan, that can be lifted again without re-running anything.

### If your team does not use exemptions, nothing changes

Same guarantee as cut-offs above, and for the same reason. There is no default row, and
`ResourceExemptionService.decorate` adds nothing at all when the asset has no exemption, so a
caller who never creates one gets byte-for-byte identical responses.

Critically, an exemption **never rewrites the scan**. `compliance`, `status` and `scanResults` are
left exactly as the run produced them. The failure and the decision to excuse it are two separate
facts, and both survive — a report that quietly turned failures into passes would be worthless as
evidence, and any consumer ignoring `exempted` still sees the unvarnished scan.

### Setting one

```
GET    /governance/v1/resource-exemptions?assetType=APIGEE[&scanKind=COMPLIANCE]
PUT    /governance/v1/resource-exemptions/{scanKind}   { assetType, assetName, reason, updatedBy }
DELETE /governance/v1/resource-exemptions/{scanKind}?assetType=APIGEE&assetName=my-proxy
```

`scanKind` is one of `COMPLIANCE`, `OWASP`, `LINTING`. Exemptions are scoped per kind on purpose:
forgiving lint noise on a proxy must not quietly forgive its OWASP findings too.

`PUT` is idempotent — exempting something already exempt refreshes who, when and why rather than
failing on the unique index, so a client retrying after a dropped response is not punished for it.

### What appears on scan history once set

Four additive fields, alongside the untouched `compliance` and `status`:

| Field | Meaning |
| --- | --- |
| `exempted` | `true`; absent entirely when the asset is not exempt |
| `exemptionReason` | why, if one was given |
| `exemptedBy` | who set it |
| `exemptedAt` | when |

How clients are expected to read them is a presentation choice, not a service one. The reference
UI shows an exempted **failing** asset as "Exempted", and an exempted **passing** asset as its real
verdict rendered inert — there is nothing to set aside, so the exemption is shown as doing nothing
rather than dressed up as a second kind of pass.

## Cloud Run Deployment
This generated project includes GitHub Actions CI/CD for Google Cloud Run.

- Workflow: `.github/workflows/ci-cd.yml`
- Service name: `probestack-compliance-service-api`
- GCP project: `probestack-prod`
- Region: `us-central1`
- Artifact Registry repository: `us-central1-docker.pkg.dev/probestack-prod/probestack-prod-apps`

On every GitHub push, the workflow builds the application, publishes a Docker image, deploys to Cloud Run, and verifies `/actuator/health`.

See `DEPLOYMENT.md` for setup instructions.
