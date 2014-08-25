ps -ef | grep -i java | grep -i org.apache.karaf.main.Main | awk '{ print $2 }' | xargs kill
