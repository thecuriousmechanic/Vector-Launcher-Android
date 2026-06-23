package com.vector.launch

object SearchRepository {

    fun search(
        query: String
    ): List<SearchResult> {

        val results =
            mutableListOf<SearchResult>()

        ProjectRepository.projects.values.forEach { project ->

            if (
                project.name.contains(
                    query,
                    ignoreCase = true
                )
            ) {

                results.add(
                    SearchResult(
                        title = project.name,
                        type = "Project",
                        project = project.name
                    )
                    
                )
            }

            project.tasks.forEach { task ->

                if (
                    task.title.contains(
                        query,
                        ignoreCase = true
                    )
                ) {

                    results.add(
                        SearchResult(
                            title = task.title,
                            type = "Task",
                            project = project.name
                        )
                    )
                }
            }

            project.notes.forEach { note ->

                if (
                    note.title.contains(
                        query,
                        ignoreCase = true
                    )
                ) {

                    results.add(
                        SearchResult(
                            title = note.title,
                            type = "Note",
                            project = project.name
                        )
                    )
                }
            }
        }

        return results
    }
}