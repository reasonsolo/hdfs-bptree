#!/usr/bin/env python
from hive_service import ThriftHive
from hive_service.ttypes import HiveServerException
from thrift import Thrift
from thrift.transport import TSocket, TTransport
from thrift.protocol import TBinaryProtocol
import sys

HIVE_HOST = "localhost"
HIVE_PORT = 10000

HIVE_OFFSET_QUERY = "SELECT %(col)s, INPUT__FILE__NAME, \
    COLLECT_SET(BLOCK__OFFSET__INSIDE__FILE) FROM %(table)s GROUP BY %(col)s, INPUT__FILE__NAME"

class HiveClientError(Exception):
  pass

class HiveClient:
  def __init__(self, host, port):
    self.host = host
    self.port = port

  def connect(self):
    try:
      if self.client:
        return
    except AttributeError:
      pass
    try:
      socket = TSocket.TSocket(self.host, self.port)
      transport =  TTransport.TBufferedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)
      self.client = ThriftHive.Client(protocol)
      transport.open()
    except Thrift.TException as te:
      raise HiveClientError('Failed to connect to Thrift server\n' +  te.message)

  def disconnect(self):
    # TODO
    self.client = None

  def execute(self, query, n = 0):
    try:
      if self.client:
        pass
    except AttributeError:
      raise HiveClientError("Client is not connected to any Thrift server")
    print("hive> %s;" % query)
    self.client.execute(query)
    if n <= 0:
      return self.client.fetchAll()
    else:
      return self.client.fetchN(n)

if __name__ == '__main__':
  if len(sys.argv) < 3:
    sys.exit("Usage: %s table column" % sys.argv[0])
  arg = {}
  arg['table'] = sys.argv[1]
  arg['col'] = sys.argv[2]

  hive = HiveClient(HIVE_HOST, HIVE_PORT)
  hive.connect()
  results = hive.execute(HIVE_OFFSET_QUERY % arg)
  for result in results:
    print(result)
  print(0)


