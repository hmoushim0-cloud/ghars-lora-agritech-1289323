package sa.factory.gharsloraagritech1.di

import sa.factory.gharsloraagritech1.data.repository.GharsLoraRepositoryImpl
import sa.factory.gharsloraagritech1.domain.repository.GharsLoraRepository
import sa.factory.gharsloraagritech1.domain.usecase.*
import sa.factory.gharsloraagritech1.hardware.HardwareSensorDriver
import sa.factory.gharsloraagritech1.ui.viewmodel.GharsLoraViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: GharsLoraRepository = GharsLoraRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): GharsLoraViewModel = GharsLoraViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
