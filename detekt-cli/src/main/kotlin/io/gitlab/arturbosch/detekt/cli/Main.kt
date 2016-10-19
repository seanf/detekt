package io.gitlab.arturbosch.detekt.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import io.gitlab.arturbosch.detekt.core.Detekt
import io.gitlab.arturbosch.detekt.core.PathFilter
import java.nio.file.Path

/**
 * @author Artur Bosch
 */
private class CLI {

	@Parameter(names = arrayOf("--project", "-p"), required = true,
			converter = PathConverter::class, description = "Project path to analyze (path/to/project).")
	lateinit var project: Path

	@Parameter(names = arrayOf("--filters", "-f"), description = "Path filters defined through regex with separator ';' (\".*test.*\").")
	val filters: String = "" // Using a converter for List<PathFilter> resulted into a ClassClassException

	@Parameter(names = arrayOf("--config", "-c"), description = "Path to the config file (path/to/config).",
			converter = PathConverter::class)
	var config: Path? = null

	@Parameter(names = arrayOf("--help", "-h"), help = true, description = "Shows the usage.")
	var help: Boolean = false

}

fun main(args: Array<String>) {
	val cli = parseAndValidateArgs(args)
	val pathFilters = cli.filters.split(";").map(::PathFilter)
	val results = Detekt(cli.project, pathFilters = pathFilters).run()
	printFindings(results)
}

private fun parseAndValidateArgs(args: Array<String>): CLI {
	val cli = CLI()
	val jCommander = JCommander(cli)
	jCommander.setProgramName("detekt")

	try {
		jCommander.parse(*args)
	} catch (ex: ParameterException) {
		println(ex.message)
		println()
		jCommander.usage()
		System.exit(-1)
	}

	if (cli.help) {
		jCommander.usage()
		System.exit(-1)
	}
	return cli
}