# Vector Launcher — Product Definition

## 1. What Is Vector Launcher?

**Vector Launcher is a minimal, productivity-first launcher for Android.**

It is intended to be the user's starting point when they begin using their device. Instead of making the user pass through a conventional app grid, a cluttered home screen, or a collection of productivity applications, Vector Launcher provides a focused environment where the user can immediately see what matters and access what they need.

The fundamental interaction is:

> **OPEN → SEE → CHOOSE → WORK**

Vector Launcher is not another productivity application sitting alongside dozens of other applications.

It is intended to become the **starting layer through which the user interacts with their device**.

---

## 2. Why Am I Making It?

The primary reason for making Vector Launcher is simple:

> **Modern devices give users access to an enormous number of applications and capabilities, but the starting experience is often cluttered, distracting, and inefficient.**

A launcher should not merely be an app drawer.

It should help the user answer:

- What do I need to do?
- What is important today?
- What am I currently working on?
- Which projects am I involved in?
- Which applications do I use most?
- What should I open right now?

Vector Launcher is being built to answer those questions **without turning the launcher itself into another complicated productivity suite.**

---

## 3. Core Philosophy

### Minimalism

The interface should contain only what is useful.

Avoid unnecessary pages, excessive navigation, decorative UI, unnecessary cards, redundant controls, heavy animations, and visual clutter.

Minimalism is not the absence of functionality. It is the removal of everything that does not contribute to the user's immediate objective.

### Productivity First

Vector Launcher exists primarily to help users **start doing things faster**.

The launcher should make it easy to see priorities, access projects and notes, access preferred applications, create tasks/notes/projects, continue existing work, and launch applications.

The launcher should not encourage users to spend time configuring the launcher instead of using their device.

### Fast

The application should feel immediate when opening the launcher, viewing the Dashboard, opening projects/tasks, scrolling sections, navigating applications, changing settings, changing wallpaper, or using alphabet navigation.

Performance is a product feature.

### Low Battery Consumption

Vector Launcher is intended to remain present throughout normal device usage. It must avoid unnecessary polling, background loops, wakeups, timers, high-frequency recomposition, continuous animations, unnecessary database observation, and repeated bitmap processing.

When idle, the launcher should be as close to passive as reasonably possible.

### Broad Device Compatibility

The application is intended for broad Android usage rather than only high-end hardware. It should account for different RAM capacities, screen sizes, aspect ratios, Android versions, display densities, and navigation systems.

---

## 4. The Main Experience

The primary Vector Launcher experience is the **Dashboard**.

The Dashboard is not intended to be a conventional dashboard full of cards. It is a lightweight starting surface.

The conceptual structure is:

```text
                    TIME

------------------------------------------------

TODAY / PRIORITY

[ ] Important task
[✓] Completed task

------------------------------------------------

PROJECTS

Project A
Project B
Project C

------------------------------------------------

NOTES

Note A
Note B
Note C

------------------------------------------------

PREFERRED APPS

★
A
B
C
D
...
Z
```

The exact visual layout may evolve, but the principle should remain:

> **One primary place containing the information and actions that matter most.**

---

## 5. Dashboard

The Dashboard should answer:

> **"What matters right now?"**

It can contain:

- **Time** — a simple, highly readable clock.
- **Today / Priority** — tasks specifically relevant to the current day or priorities.
- **Projects** — access to larger pieces of work.
- **Notes** — lightweight persistent information.
- **Preferred Apps** — applications selected by the user for immediate access.

Task representation:

```text
[ ] Task
[✓] ~~Completed Task~~
```

Priority remains semantic:

- **High → Red**
- **Medium → Yellow/Amber**
- **Low → Green**

Completed tasks remain visible and move below incomplete tasks.

---

## 6. Projects

Projects group related work.

A project can contain:

- project name
- description
- tasks
- notes
- resources
- goals
- relevant history/timeline

Projects should remain lightweight and contextual rather than becoming a full project-management suite.

### Project isolation

A task or note created inside a project belongs to that project. It should **not automatically appear in the Dashboard's Today/Priority section** merely because it exists.

Project-scoped work remains project-scoped unless the user explicitly makes it relevant to the Dashboard/Today context.

Users must be able to:

- create a project
- rename/edit a project
- add tasks
- add notes
- delete a project

---

## 7. Tasks

Tasks represent actionable work.

The fundamental representation is:

```text
[ ] Task
```

When completed:

```text
[✓] ~~Task~~
```

The square brackets are literal characters.

Completed tasks:

- remain visible
- move below incomplete tasks
- retain priority meaning
- remain persistent

Tasks may belong to a project or exist independently.

A project task must remain associated with that project.

---

## 8. Notes

Notes are lightweight pieces of information.

They should be easy to create, edit, read, and associate with a project.

Notes should not automatically appear in unrelated Dashboard contexts.

The system should preserve context instead of flattening everything into one global list.

---

## 9. Applications

Vector Launcher is also an application launcher.

The user decides which applications appear in **Preferred Apps**.

There is also an **All Apps** pathway for accessing installed applications.

Preferred Apps are not intended to be another full application grid. They are the applications deliberately chosen for immediate access.

### Alphabet Navigation

The application list uses alphabetical navigation:

```text
                    ★
                    A
                    B
                    C
                    D
                    E
                    F
                    .
                    .
                    Z
```

The user can drag their finger along the right-side navigation. Reaching `C`, for example, should immediately move the application list toward applications beginning with C.

The interaction should feel physical:

```text
Finger
   ↓
Wave
   ↓
Stretch
   ↓
Letter selection
   ↓
Application list movement
```

The navigation must be functional, not decorative.

---

## 10. User Customization

Vector Launcher should allow personalization without becoming a theme-management application.

The user should be able to control:

- **Wallpaper** — the user chooses it.
- **Theme** — a coherent Amber visual identity.
- **Accent / Font Color** — the primary interface color can be customized.
- **Brightness** — visual brightness.
- **Contrast** — visual contrast.
- **Saturation / Wallpaper Dimming** — where technically efficient.

Customization must not compromise performance.

---

## 11. What Vector Launcher Is NOT

Vector Launcher is deliberately not intended to become:

- a full operating system
- a replacement for Android
- a social network
- Slack
- a full project-management suite
- a dedicated notes application
- a full calendar application
- a universal search engine
- an AI assistant
- a complicated widget marketplace
- a visually elaborate home-screen customization tool

It may integrate with other systems in the future, but its identity must remain that of a **minimal productivity launcher**.

---

## 12. Functionality Behind Simplicity

Vector Launcher may contain substantial engineering underneath the interface:

- persistent local data
- project relationships
- task relationships
- notes
- resources
- application indexing
- user preferences
- wallpaper processing
- adaptive layouts
- gesture navigation

But complexity should exist **behind the interface**, not in front of the user.

The desired experience is:

> **Simple interface → powerful underlying system**

not:

> **Complex interface → user has to manage the complexity**

---

## 13. Local-First Philosophy

Vector Launcher should work primarily from the user's device.

Core functionality should not depend on a permanent network connection.

The launcher should remain useful when offline, temporarily disconnected, or operating under poor network conditions.

Local data should remain immediately accessible.

Cloud functionality, if introduced later, should enhance the experience rather than make the core launcher unusable.

---

## 14. Architecture Principles

### Stable Identity

Core entities use stable IDs rather than titles:

```text
Project.id
Task.id
Note.id
Resource.id
```

Titles are mutable. Relationships should not depend on names.

### Persistent State

User configuration must survive application restarts, including:

- preferred applications
- wallpaper
- accent color
- brightness
- contrast
- widget visibility
- widget order
- project data
- task data
- note data

### Reactive Updates

Changes should appear immediately without unnecessary application restarts.

### Context Preservation

Data should retain its intended scope.

For example:

```text
Project
   ↓
Project Task
```

does not automatically mean:

```text
Project Task
   ↓
Dashboard Today
```

Context must be intentional.

---

## 15. Performance Philosophy

Performance targets are engineering constraints.

Priorities:

1. fast startup
2. low idle CPU
3. low background activity
4. low memory overhead
5. low battery consumption
6. efficient rendering
7. minimal allocations during interaction

Avoid optimizing only for benchmark numbers.

The actual goal is:

> **The user should never feel Vector Launcher working in the background.**

---

## 16. Why the Launcher Format?

A productivity application requires intentional navigation.

A launcher is different: it is accessed frequently and becomes part of the user's normal device behavior.

Vector Launcher aims to reduce the distance between:

```text
I need to do something
```

and:

```text
I am doing it
```

The fewer unnecessary steps between those states, the more useful the launcher becomes.

This is the fundamental product opportunity.

---

## 17. Long-Term Direction

Vector Launcher should evolve through refinement rather than feature accumulation.

Future development should follow this question:

> **Does this make the user's transition from intention to action faster, clearer, or less distracting?**

If yes, it may belong in Vector Launcher.

If not, it should probably remain outside the core product.

The product should become:

- faster
- simpler
- more reliable
- more adaptable
- more useful

without becoming:

- heavier
- more cluttered
- more distracting
- more complicated

---

## 18. Product Definition

> **Vector Launcher is a minimal, fast, low-power Android productivity launcher that turns the device's starting screen into a focused gateway to the user's tasks, projects, notes, and most-used applications.**

Its purpose is not to give the user more things to manage.

Its purpose is to help the user **get to what matters and start working immediately.**

---

## 19. Non-Negotiable Principles

1. **Minimalism over decoration.**
2. **Productivity over feature count.**
3. **Speed over visual complexity.**
4. **Low battery consumption over unnecessary background activity.**
5. **User control over forced defaults.**
6. **Context preservation over indiscriminate global lists.**
7. **One primary launcher surface over unnecessary navigation.**
8. **Powerful internals with a simple interface.**
9. **Broad Android compatibility over device-specific optimization.**
10. **Every visible control must work.**
11. **No dead UI.**
12. **No feature should exist merely because it can be implemented.**

---

# Final Statement

Vector Launcher is being made to solve a simple problem:

**The starting point of a device should help the user start, not distract the user.**

It should open quickly, show what matters, provide immediate access to the user's chosen applications, preserve context around their work, consume very little battery, and stay out of the way.

The ideal Vector Launcher experience is:

```text
OPEN
  ↓
SEE WHAT MATTERS
  ↓
CHOOSE
  ↓
START
  ↓
WORK
```

Everything in the product should serve that sequence.
