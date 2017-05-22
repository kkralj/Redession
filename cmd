sudo systemctl restart network-manager.service


mvn exec:java -Dexec.mainClass="rat.client.ClientMain"
mvn exec:java -Dexec.mainClass="rat.master.MasterMain"

