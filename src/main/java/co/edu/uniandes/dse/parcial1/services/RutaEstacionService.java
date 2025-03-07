package co.edu.uniandes.dse.parcial1.services;

import org.modelmapper.spi.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.uniandes.dse.parcial1.entities.EstacionEntity;
import co.edu.uniandes.dse.parcial1.entities.RutaEntity;
import co.edu.uniandes.dse.parcial1.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.parcial1.repositories.EstacionRepository;
import co.edu.uniandes.dse.parcial1.repositories.RutaRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
public class RutaEstacionService {
    @Autowired
    private RutaRepository rutaRepository;
    @Autowired
    private EstacionRepository estacionRepository;

    @Transactional
    public void removeEstacionRuta(Long idEstacion, Long idRuta) throws Exception {
        log.info("Inicia proceso de eliminar una estacion con id {0} ala ruta con id = {1}", idEstacion, idRuta);
		
        Optional<RutaEntity> rutaEntity = rutaRepository.findById(idRuta);
		Optional<EstacionEntity> estacionEntity = estacionRepository.findById(idEstacion);

		if (rutaEntity.isEmpty())
			throw new EntityNotFoundException("Entity not found");

		if (estacionEntity.isEmpty())
			throw new EntityNotFoundException("Entity not found");
        int i = 0;
        for(RutaEntity ruta :estacionEntity.get().getRutas()){
            if (ruta.getTipo().equals("nocturna")){
                i++;
            }
        }
        if (i == 1 && rutaEntity.get().getTipo().equals("nocturna")){
            throw new Exception("Una estacion no puede no tener una ruta nocturna");
        }

		rutaEntity.get().getEstaciones().remove(estacionEntity.get());
        estacionEntity.get().getRutas().remove(rutaEntity.get());
        log.info("Termina proceso de eliminar una estacion con id {0} ala ruta con id = {1}", idEstacion, idRuta);
    }
    @Transactional
    public EstacionEntity addEstacionRuta(Long idEstacion, Long idRuta) throws Exception {
        log.info("Inicia proceso de agregar una estacion con id {0} a la ruta con id = {1}", idEstacion, idRuta);
		
        Optional<RutaEntity> rutaEntity = rutaRepository.findById(idRuta);
		Optional<EstacionEntity> estacionEntity = estacionRepository.findById(idEstacion);

		if (rutaEntity.isEmpty())
			throw new EntityNotFoundException("Entity not found");

		if (estacionEntity.isEmpty())
			throw new EntityNotFoundException("Entity not found");
        int i = 0;
        for(RutaEntity ruta :estacionEntity.get().getRutas()){
            if (ruta.getTipo().equals("circular")){
                i++;
            }
        }
        if (i >=2 && estacionEntity.get().getCapacidad()<100){
            throw new Exception("Una estacion no puede tener dos rutas circulares con capacidad menor a 100");
        }

		rutaEntity.get().getEstaciones().add(estacionEntity.get());
        estacionEntity.get().getRutas().add(rutaEntity.get());
        log.info("Termina proceso de agregar una estacion con id {0} a la ruta con id = {1} ", idEstacion, idRuta);
        return estacionEntity.get();
    }
}
