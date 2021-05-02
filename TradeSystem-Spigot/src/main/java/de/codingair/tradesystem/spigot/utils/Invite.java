package de.codingair.tradesystem.spigot.utils;

import java.util.Objects;

public class Invite {
    private final String name;
    private final boolean proxyInvite;

    public Invite(String name) {
        this(name, false);
    }

    public Invite(String name, boolean proxyInvite) {
        this.name = name;
        this.proxyInvite = proxyInvite;
    }

    public String getName() {
        return name;
    }

    public boolean isProxyInvite() {
        return proxyInvite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (!(o instanceof String) && getClass() != o.getClass())) return false;

        if (o instanceof String) {
            String invite = (String) o;
            return this.name.equalsIgnoreCase(invite);
        }

        Invite invite = (Invite) o;
        return this.name.equalsIgnoreCase(invite.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
