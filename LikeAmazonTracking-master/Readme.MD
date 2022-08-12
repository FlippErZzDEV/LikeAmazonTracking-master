# Setup:
Da das Mitcompilen der Datenbank derzeit zu problemen führt, muss die Datenbank: main.src/session.db Manuell in den Plugin-Ordner: LikeAmazonTracking abgelegt werden.

# Permissions:
- staticservice.buildserver.lat.session.info.current -> /lat info + /lat session current
- staticservice.buildserver.lat.session.info.own -> /lat info + /lat session <ID> (OwnSessions) + /lat session list <ownName> <side>
- staticservice.buildserver.lat.session.info.all -> /lat info player + /lat session <ID> (allPlayer) + /lat session list <player|*> <side>
- staticservice.buildserver.lat.session.changereason.all -> /lat session <ID> (allPlayer) setreason <ReasonID>
- staticservice.buildserver.lat.session.changereason.own -> /lat session <ID> (OwnSession) setreason <ReasonID>
- staticservice.buildserver.lat.session.changereason.current -> /lat session current setreason <ReasonID>
- staticservice.buildserver.lat.reason.info -> /lat reason list <side>
- staticservice.buildserver.lat.reason.prepare.text.all -> /lat reason <ReasonID> (all) text <text>
- staticservice.buildserver.lat.reason.prepare.text.own -> /lat reason <ReasonID> (whenPlayerIsMaintainer) text <text>
- staticservice.buildserver.lat.reason.prepare.maintainer -> /lat reason <ReasonID> setMaintainer <Player|UUID>
- staticservice.buildserver.lat.reason.create -> /lat reason new