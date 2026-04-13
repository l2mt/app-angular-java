@clientes
Feature: Clientes API

  Background:
    * def System = Java.type('java.lang.System')
    * def UUID = Java.type('java.util.UUID')
    * url System.getProperty('karate.baseUrl')
    * configure headers = { Accept: 'application/json', Content-Type: 'application/json' }
    * def suffix = UUID.randomUUID().toString().replaceAll('-', '').substring(0, 10)

  Scenario: crear cliente
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Cliente " + suffix)',
      genero: 'MASCULINO',
      edad: 30,
      identificacion: '#("CL" + suffix)',
      direccion: 'Quito',
      telefono: '#("099" + suffix.substring(0, 7))',
      contrasena: '1234',
      estado: true
    }
    """
    When method post
    Then status 201
    And match response.clienteId == '#number'
    And match response.identificacion == '#("CL" + suffix)'

  Scenario: obtener cliente por id
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Get " + suffix)',
      genero: 'FEMENINO',
      edad: 28,
      identificacion: '#("GC" + suffix)',
      direccion: 'Ambato',
      telefono: '#("098" + suffix.substring(0, 7))',
      contrasena: '1234',
      estado: true
    }
    """
    When method post
    Then status 201
    * def clienteId = response.clienteId
    Given path 'clientes', clienteId
    When method get
    Then status 200
    And match response.clienteId == clienteId
    And match response.identificacion == '#("GC" + suffix)'

  Scenario: actualizar cliente con patch
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Patch " + suffix)',
      genero: 'OTRO',
      edad: 35,
      identificacion: '#("PC" + suffix)',
      direccion: 'Cuenca',
      telefono: '#("097" + suffix.substring(0, 7))',
      contrasena: '1234',
      estado: true
    }
    """
    When method post
    Then status 201
    * def clienteId = response.clienteId
    Given path 'clientes', clienteId
    And request
    """
    {
      direccion: 'Duran',
      telefono: '0991112233',
      estado: false
    }
    """
    When method patch
    Then status 200
    And match response.clienteId == clienteId
    And match response.direccion == 'Duran'
    And match response.telefono == '0991112233'
    And match response.estado == false

  Scenario: eliminar cliente
    Given path 'clientes'
    And request
    """
    {
      nombre: '#("Karate Delete " + suffix)',
      genero: 'MASCULINO',
      edad: 26,
      identificacion: '#("DC" + suffix)',
      direccion: 'Loja',
      telefono: '#("096" + suffix.substring(0, 7))',
      contrasena: '1234',
      estado: true
    }
    """
    When method post
    Then status 201
    * def clienteId = response.clienteId
    Given path 'clientes', clienteId
    When method delete
    Then status 204
