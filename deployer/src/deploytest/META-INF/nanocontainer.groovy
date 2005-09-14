builder = new org.nanocontainer.script.groovy.GroovyNodeBuilder()
pico = builder.container(parent:parent) {
 component(instance:'Groovy')
 component(key:'zap', class:'foo.bar.Zap')
}
