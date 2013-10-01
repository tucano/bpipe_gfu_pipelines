// TESTING SINGLE STAGE PARAMETERS
hello = {
	var WORLD : "world"

    doc "Run hello with to $WORLD"

    exec """
        echo hello $WORLD
    """
}

