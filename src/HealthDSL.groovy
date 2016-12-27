/**
 * Created by slobolai on 12/16/2016.
 */
class HealthDSL {
    static void main(String[] args) {
        runDslFile(new File("health.groovy"));
    }

    static void runDslFile(File definition) {
        Script dslScript = new GroovyShell().parse(definition.text)
        println definition.text
    }
}

class Rule {

}

class Configuration {
    def directory;
    boolean doCyclicDependencyTest;
    boolean throwExceptionWhenNoPackages;
    void addSource (SourceDirectory directory) {
        this.directory = directory;
    }

    void doCyclicDependencyTest(boolean doCyclicDependencyTest) {
        this.doCyclicDependencyTest = doCyclicDependencyTest
    }

    void throwExceptionWhenNoPackages(boolean throwExceptionWhenNoPackages) {
        this.throwExceptionWhenNoPackages = throwExceptionWhenNoPackages
    }
}

class SourceDirectory {

}

class ArchitectureDelegate {
    private Configuration configuration

    ArchitectureDelegate(Configuration configuration) {
        this.configuration = configuration
    }

    void setAllowCyclicDependencies(boolean allowCyclicDependencies) {
        this.configuration.doCyclicDependencyTest = !allowCyclicDependencies
    }

    void setAllowClassesWithoutPackage(boolean allowClassesWithoutPackage) {
        this.configuration.throwExceptionWhenNoPackages = !allowClassesWithoutPackage
    }

    void classes(String name, boolean ignore) {
        this.configuration.addSource new SourceDirectory(name, !ignore)
    }

    void classes(String name) {
        classes name, false
    }

    void jar(String name, boolean ignore) {
        classes name, ignore
    }

    void jar(String name) {
        classes name
    }

    void rules(Closure cl) {
        cl.delegate = new RulesDelegate(configuration)
        cl.resolveStrategy = Closure.DELEGATE_FIRST

        cl()
    }

    def propertyMissing(String name) {
        return null
    }
}

class RulesDelegate {
    private Configuration configuration

    RulesDelegate(Configuration configuration) {
        this.configuration = configuration
    }

    def methodMissing(String name, Object args) {
        if (args.length == 1) {
            if (args[0] instanceof Closure) {
                Rule rule = new Rule(name)

                args[0].delegate = new RuleDelegate(rule)
                args[0].resolveStrategy = Closure.DELEGATE_FIRST

                args[0]()

                this.configuration.addRule rule
            } else {
                throw new MissingMethodException(name, this.class, args as Object[])
            }
        } else {
            throw new MissingMethodException(name, this.class, args as Object[])
        }
    }
}

class RuleDelegate {
    private Rule rule

    RuleDelegate(Rule rule) {
        this.rule = rule
    }

    void setComment(String comment) {
        this.rule.comment = comment
    }

    void methodMissing(String name, Object args) {
        if (name == "package") {
            if (args.length == 1 && args[0] instanceof String) {
                this.rule.addPackage args[0]
            } else {
                throw new MissingMethodException(name, this.class, args as Object[])
            }
        } else {
            throw new MissingMethodException(name, this.class, args as Object[])
        }
    }

    void violation(String violation) {
        this.rule.addViolation violation
    }

    def propertyMissing(String name) {
        return null
    }
}
