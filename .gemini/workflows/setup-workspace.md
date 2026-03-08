---
description: Wire up global agent skills, rules, and workflows in the current workspace
---

# Setup Workspace

// turbo-all

## 1. Create Global Agent Symlink
```bash
ln -sfn ~/.agent .agent
```

## 2. Verify
```bash
echo "Skills: $(ls .agent/skills/ | wc -l)"
echo "Workflows: $(ls .agent/workflows/*.md | wc -l)"
echo "Rules: $(ls .agent/rules/AGENT_RULES.md && echo 'OK')"
```

This links the global `~/.agent` directory (skills, rules, workflows) into the current workspace so they are automatically discovered.
