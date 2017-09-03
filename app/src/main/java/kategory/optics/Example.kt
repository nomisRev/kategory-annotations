package kategory.optics

import kategory.Lens
import kategory.lenses

@lenses data class Street(val number: Int, val name: String)
@lenses data class Address(val city: String, val street: Street)
@lenses data class Company(val name: String, val address: Address)
@lenses data class Employee(val name: String, val company: Company)

val employee = Employee("John", Company("Awesome inc", Address("Awesome town", Street(23, "awesome street"))))

val employee1 = employee.copy(
        company = employee.company.copy(
                address = employee.company.address.copy(
                        street = employee.company.address.street.copy(
                                name = employee.company.address.street.name.capitalize()
                        )
                )
        )
)

val employeeStreetNameLens: Lens<Employee, String> = employeeCompany() composeLens companyAddress() composeLens addressStreet() composeLens streetName()

val employee2 = employeeStreetNameLens.modify({ name -> name.capitalize() }, employee)