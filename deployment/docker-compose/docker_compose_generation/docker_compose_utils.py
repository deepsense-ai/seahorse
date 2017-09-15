# Copyright (c) 2016, CodiLime Inc.

import socket
import struct


class Services(object):
    def __init__(self):
        self.services = []

    def add_service(self, service):
        self.services.append(service)

    def __getattr__(self, item):
        return [s for s in self.services if s.name().lower() == item.lower()][0]


class Env(object):
    def __init__(self, **kwargs):
        self.d = dict(**kwargs)

    def __add__(self, other):
        if isinstance(other, dict):
            self.d.update(other)
        elif isinstance(other, Env):
            self.d.update(other.d)

        return self

    def iteritems(self):
        return self.d.iteritems()

    def to_dict(self):
        return self.d


class Subnet(object):
    def __init__(self, subnet):
        s = subnet.split("/", 1)
        self.ip = s[0]
        self.mask_length = s[1]
        self.ip = Subnet.masked_ip(self.ip, int(self.mask_length))

    def as_string(self):
        return "{}/{}".format(self.ip, self.mask_length)

    def __str__(self):
        return self.as_string()

    def __repr__(self):
        return "Subnet('{}')".format(self.as_string())

    def default_gateway(self):
        return socket.inet_ntoa(
            struct.pack(
                "!I", struct.unpack("!I", socket.inet_aton(self.ip))[0] + 1))

    @classmethod
    def masked_ip(cls, ip, mask_length):
        mask_length = int(mask_length)
        mask = int("1" * mask_length + "0" * (32 - mask_length), 2)
        masked_ip_int = struct.unpack("!I", socket.inet_aton(ip))[0] & mask

        return socket.inet_ntoa(struct.pack("!I", masked_ip_int))


class Address(object):
    def __init__(self, host, port):
        self.host = host
        self.port = port

    def as_env(self, host_name, port_name):
        return Env(**{
            host_name: self.host,
            port_name: self.port
        })

    def as_string(self):
        return '{}:{}'.format(self.host, self.port)

    def __str__(self):
        return self.as_string()


class PortMappings(object):
    class Mapping(object):
        def __init__(self, internal, exposed):
            self.internal = internal
            self.exposed = exposed

    def __init__(self):
        self.mappings = {}

    def add(self, mapping, name=None):
        assert isinstance(mapping, PortMappings.Mapping)
        assert name not in self.mappings, 'no overwrites allowed'
        self.mappings[name] = mapping
        return self

    def get(self, name=None):
        return self.mappings[name]

    def __iter__(self):
        for m in self.mappings.itervalues():
            yield m


class Credentials(object):
    def __init__(self, user, password, default_user_env_name, default_password_env_name):
        self.user = user
        self.password = password
        self.default_user_env_name = default_user_env_name
        self.default_password_env_name = default_password_env_name

    def as_env(self, user_name=None, password_name=None):
        user_name = user_name or self.default_user_env_name
        password_name = password_name or self.default_password_env_name
        return Env(**{
            user_name: self.user,
            password_name: self.password
        })


class Ports(object):
    @staticmethod
    def exposed_on_localhost(external, internal):
        return '127.0.0.1:{}:{}'.format(external, internal)


class Repositories(object):
    backend = 'backend'
    frontend = 'frontend'
