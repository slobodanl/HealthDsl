architecture {
    // cyclic dependency check enabled by default
    jar "spring.jar"
    rules {
        "beans-web" {
            comment = "org.springframework.beans.factory cannot depend on org.springframework.web"
            'package' "org.springframework.beans"
            violation "org.springframework.web"
        }
        "must-fail" {
            comment = "org.springframework.orm.hibernate3 cannot depend on org.springframework.core.io"
            'package' "org.springframework.orm.hibernate3"
            violation "org.springframework.core.io"
        }
    }
}