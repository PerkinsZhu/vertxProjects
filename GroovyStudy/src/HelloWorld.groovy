import groovy.xml.MarkupBuilder

class HelloWorld {
    static void main(args) {
        println("-------------")

        def a = 1..10
        a.each { i ->
            println("${i}")
        }
        12.each {
            println("i am $it")
        }
        (1..10).each { i ->
            println("${i}")
        }
    }
    def employeename
    def student1
    def student_name
    def xml = new MarkupBuilder()


}
