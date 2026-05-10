interface Flyable {
    fun fly()
}

interface Swimmable {
    fun swim()
}

interface Runnable {
    fun run()
}

class Eagle(val name: String) : Flyable {
    override fun fly() {
        println("$name is flying high.")
    }
}

class Duck(val name: String) : Flyable, Swimmable {
    override fun fly() {
        println("$name is flying over the lake.")
    }

    override fun swim() {
        println("$name is swimming on the water.")
    }
}

class Fish(val name: String) : Swimmable {
    override fun swim() {
        println("$name is swimming fast.")
    }
}

class Dog(val name: String) : Runnable {
    override fun run() {
        println("$name is running in the park.")
    }
}

class Frog(val name: String) : Swimmable, Runnable {
    override fun swim() {
        println("$name is swimming near the shore.")
    }

    override fun run() {
        println("$name is jumping quickly.")
    }
}

fun processSelectedAnimals(animal: Any) {
    when (animal) {
        is Duck -> {
            println("Processing duck: ${animal.name}")
            animal.fly()
            animal.swim()
        }

        is Dog -> {
            println("Processing dog: ${animal.name}")
            animal.run()
        }
    }
}

fun main() {
    val objects: List<Any> = listOf(
        Eagle("Eddie"),
        Duck("Donald"),
        Fish("Nemo"),
        Dog("Buddy"),
        Frog("Freddy")
    )

    val filteredObjects = objects.filter { it is Duck || it is Dog }

    filteredObjects.forEach { processSelectedAnimals(it) }
}
