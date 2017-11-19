java -ea -Xms2m -cp 'jar/*' -Djava.security.manager -Djava.security.policy=dcvm.policy fr.upmc.components.cvm.utils.DCVMCyclicBarrier context.xml
java -cp '../jar/*' -Djava.security.manager -Djava.security.policy=dcvm.policy fr.upmc.components.registry.GlobalRegistry context.xml
java -ea -Djava.security.manager -Djava.security.policy=dcvm.policy -jar test.jar "controller"  context.xml
ping 127.0.0.1 -n 500> NUL