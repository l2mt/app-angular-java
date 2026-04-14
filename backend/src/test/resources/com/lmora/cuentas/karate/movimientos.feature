@movimientos
Feature: Movimientos API

  Background:
    * def System = Java.type('java.lang.System')
    * def UUID = Java.type('java.util.UUID')
    * url System.getProperty('karate.baseUrl')
    * configure headers = { Accept: 'application/json', Content-Type: 'application/json' }
    * def suffix = UUID.randomUUID().toString().replaceAll('-', '').substring(0, 10)

  Scenario: crear movimiento
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Movimiento " + suffix)',
      genero: 'MASCULINO',
      edad: 33,
      identificacion: '#("MV" + suffix)',
      direccion: 'Guayaquil',
      telefono: '#("091" + suffix.substring(0, 7))',
      contrasena: '1234',
      estado: true
    }
    """
    When method post
    Then status 201
    * def clienteId = response.clienteId
    Given path 'cuentas'
    And request
    """
    {
      clienteId: '#(clienteId)',
      numeroCuenta: '#("MOV" + suffix)',
      tipoCuenta: 'AHORROS',
      saldoInicial: 1000.00,
      estado: true
    }
    """
    When method post
    Then status 201
    * def cuentaId = response.cuentaId
    Given path 'movimientos'
    And request
    """
    {
      cuentaId: '#(cuentaId)',
      tipoMovimiento: 'CREDITO',
      valor: 200.00
    }
    """
    When method post
    Then status 201
    And match response.movimientoId == '#number'
    And match response.cuentaId == cuentaId
    And match response.tipoMovimiento == 'CREDITO'
    And match response.valor == 200.00
    And match response.saldo == 1200.00
