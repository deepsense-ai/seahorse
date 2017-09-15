import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerExposedPorts := Seq(8080)
dockerUpdateLatest := true

lazy val addApkRepos = "echo -en 'http://dl-cdn.alpinelinux.org/alpine/edge/main\\n" +
  "http://dl-cdn.alpinelinux.org/alpine/edge/testing\\n' >> /etc/apk/repositories"

lazy val installExim = "apk update && apk add exim && mkdir -p /var/log/exim && touch /var/log/exim/mainlog && " +
  "touch /var/log/exim/paniclog && touch /var/log/exim/rejectlog && chown exim:exim /var/log/exim/*log"

lazy val configureInit = "echo -en '::respawn:/usr/sbin/exim -bd -q10m\\n" +
  "::once:/opt/docker/bin/deepsense-schedulingmanager\\n'" +
  " > /etc/inittab"

// The <; sets the delimiter (in this case the semicolon) in the subsequent hostlist.
// I added 172.18.0.0/16 net to the list, so I could connect from docker host machine to exim via SMTP.
// It can probably be removed as the machines inside the docker network are all going to be local.
// Other way to configure this is to explicitly ADD the exim.conf file to the /etc/exim directory.
lazy val enableRelayFromHosts = "sed -i 's|^[^#]*hostlist \\+relay_from_hosts *= *.*$|hostlist " +
  "relay_from_hosts = <; ::1 ; 127.0.0.1 ; 172.18.0.0/16|g' /etc/exim/exim.conf"

dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("ENV", "SMTP_PORT", "60111"),
  Cmd("RUN", addApkRepos),
  Cmd("RUN", s"$installExim && $configureInit && $enableRelayFromHosts"),
  ExecCmd("ENTRYPOINT", "/sbin/init")
)
