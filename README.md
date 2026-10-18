# SDU Booking

> A solution for students and staff looking to book rooms in SDU for self-study/club activities/study groups.

## Problem Summary
Our system will cover booking various acceptable rooms within SDU. It won’t cover letting users book any room you want at any time, some can be busy by teachers, maintenance etc.

## Team

Items in this section will be retroactively be added to weekly milestone logs.

| Name | Student ID | Role / Main contribution |
|------|-----------|--------------------------|
| Myrzatayev Nuraly | 240107075 | ADR |
| Abdrakhpan Dinmukhamed  | 240107044 | Container Diagram |
| Eralp Aliyar Sengoz | 220107150 | Repository setup, maintenance, and README |
| Orynbassar Nurpeiis | 240110009 | Architecture Diagram |

## Architecture
Selected style: Modular Monolith

![Placeholder](docs/diagrams/ArchitectureDiagram.png)
*Architecture Diagram*

![Container diagram](docs/diagrams/ContainerDiagram.png)
*Container Diagram*

See [ADR-001](docs/adr/ADR-001.md) for the first architecture decision.

## Repository Structure
| Path | Purpose |
|------|---------|
| `docs/architecture/` | Container diagram and diagram source |
| `docs/adr/` | Architecture Decision Records |
| `docs/milestones/` | Milestone reports |
| `src/` | Application source code |
| `tests/` | Automated tests |

## Status
Milestones
- 1: problem and system context.
- 2: architecture and repository setup.