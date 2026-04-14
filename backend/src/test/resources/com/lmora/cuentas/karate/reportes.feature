@reportes
Feature: Reportes API

  Background:
    * def System = Java.type('java.lang.System')
    * def UUID = Java.type('java.util.UUID')
    * url System.getProperty('karate.baseUrl')
    * configure headers = { Accept: 'application/json', Content-Type: 'application/json' }
    * def suffix = UUID.randomUUID().toString().replaceAll('-', '').substring(0, 10)

  Scenario: generar reporte
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Reporte " + suffix)',
      genero: 'FEMENINO',
      edad: 31,
      identificacion: '#("RP" + suffix)',
      direccion: 'Quito',
      telefono: '#("090" + suffix.substring(0, 7))',
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
      numeroCuenta: '#("RPT" + suffix)',
      tipoCuenta: 'AHORROS',
      saldoInicial: 500.00,
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
      valor: 100.00,
      fecha: '2026-04-10T09:00:00'
    }
    """
    When method post
    Then status 201
    Given path 'movimientos'
    And request
    """
    {
      cuentaId: '#(cuentaId)',
      tipoMovimiento: 'DEBITO',
      valor: -40.00,
      fecha: '2026-04-12T15:30:00'
    }
    """
    When method post
    Then status 201
    Given path 'reportes'
    And param clienteId = clienteId
    And param fechaDesde = '2026-04-01'
    And param fechaHasta = '2026-04-30'
    When method get
    Then status 200
    And match response.cliente.clienteId == clienteId
    And match response.totalCreditos == 100.00
    And match response.totalDebitos == 40.00
    And match response.cuentas == '#[1]'
    And match response.cuentas[0].cuentaId == cuentaId
    And match response.pdfBase64 contains 'JVBER'
