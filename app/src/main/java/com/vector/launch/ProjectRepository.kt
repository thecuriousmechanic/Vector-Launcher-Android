package com.vector.launch

object ProjectRepository {

    val projects = mapOf(

        "Build Vector Launcher" to ProjectData(
            name = "Build Vector Launcher",

            tasks = listOf(
                Task("Implement Navigation"),
                Task("Improve UI"),
                Task("Add Persistence")
            ),

            notes = listOf(
                Note("Architecture"),
                Note("Design Notes"),
                Note("Android Tasks")
            )
        ),

        "Review Research" to ProjectData(
            name = "Review Research",

            tasks = listOf(
                Task("Read Papers"),
                Task("Summarize Findings"),
                Task("Update Notes")
            ),

            notes = listOf(
                Note("Research Papers"),
                Note("Server Studies"),
                Note("AI Reports")
            )
        ),

        "Advance Renovation" to ProjectData(
            name = "Advance Renovation",

            tasks = listOf(
                Task("Review Blueprint"),
                Task("Cost Analysis"),
                Task("Material Selection")
            ),

            notes = listOf(
                Note("Blueprints"),
                Note("Cost Estimates"),
                Note("Materials")
            )
        )
    )
}