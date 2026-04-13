@cuentas
Feature: Cuentas API

  Background:
    * def System = Java.type('java.lang.System')
    * def UUID = Java.type('java.util.UUID')
    * url System.getProperty('karate.baseUrl')
    * configure headers = { Accept: 'application/json', Content-Type: 'application/json' }
    * def suffix = UUID.randomUUID().toString().replaceAll('-', '').substring(0, 10)

  Scenario: crear cuenta
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Cuenta " + suffix)',
      genero: 'FEMENINO',
      edad: 30,
      identificacion: '#("CC" + suffix)',
      direccion: 'Quito',
      telefono: '#("095" + suffix.substring(0, 7))',
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
      numeroCuenta: '#("CTA" + suffix)',
      tipoCuenta: 'AHORROS',
      saldoInicial: 1500.00,
      estado: true
    }
    """
    When method post
    Then status 201
    And match response.cuentaId == '#number'
    And match response.clienteId == clienteId
    And match response.numeroCuenta == '#("CTA" + suffix)'

  Scenario: obtener cuenta por id
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Get Cuenta " + suffix)',
      genero: 'MASCULINO',
      edad: 32,
      identificacion: '#("GC" + suffix)',
      direccion: 'Manta',
      telefono: '#("094" + suffix.substring(0, 7))',
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
      numeroCuenta: '#("GET" + suffix)',
      tipoCuenta: 'CORRIENTE',
      saldoInicial: 900.00,
      estado: true
    }
    """
    When method post
    Then status 201
    * def cuentaId = response.cuentaId
    Given path 'cuentas', cuentaId
    When method get
    Then status 200
    And match response.cuentaId == cuentaId
    And match response.numeroCuenta == '#("GET" + suffix)'

  Scenario: actualizar cuenta con patch
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Patch Cuenta " + suffix)',
      genero: 'OTRO',
      edad: 29,
      identificacion: '#("PA" + suffix)',
      direccion: 'Cuenca',
      telefono: '#("093" + suffix.substring(0, 7))',
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
      numeroCuenta: '#("PAT" + suffix)',
      tipoCuenta: 'AHORROS',
      saldoInicial: 700.00,
      estado: true
    }
    """
    When method post
    Then status 201
    * def cuentaId = response.cuentaId
    Given path 'cuentas', cuentaId
    And request
    """
    {
      numeroCuenta: '#("NEW" + suffix)',
      estado: false
    }
    """
    When method patch
    Then status 200
    And match response.cuentaId == cuentaId
    And match response.numeroCuenta == '#("NEW" + suffix)'
    And match response.estado == false

  Scenario: eliminar cuenta
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Delete Cuenta " + suffix)',
      genero: 'FEMENINO',
      edad: 27,
      identificacion: '#("DL" + suffix)',
      direccion: 'Loja',
      telefono: '#("092" + suffix.substring(0, 7))',
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
      numeroCuenta: '#("DEL" + suffix)',
      tipoCuenta: 'AHORROS',
      saldoInicial: 500.00,
      estado: true
    }
    """
    When method post
    Then status 201
    * def cuentaId = response.cuentaId
    Given path 'cuentas', cuentaId
    When method delete
    Then status 204
