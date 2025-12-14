rootProject.name = "ecologique"

// Include the main backend application build.
// This allows the IDE to see the backend, its submodules (products, users),
// and the mapped common modules as a single project structure.
includeBuild("deployables/backend")

// Explicitly include build-logic so it appears as a top-level module in the IDE project view.
// This makes editing convention plugins easier without diving into nested structures.
includeBuild("build-logic")
