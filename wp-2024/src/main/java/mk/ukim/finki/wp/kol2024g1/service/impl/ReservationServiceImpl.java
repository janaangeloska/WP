package mk.ukim.finki.wp.kol2024g1.service.impl;

import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.model.Reservation;
import mk.ukim.finki.wp.kol2024g1.model.RoomType;
import mk.ukim.finki.wp.kol2024g1.model.exceptions.InvalidReservationIdException;
import mk.ukim.finki.wp.kol2024g1.repository.ReservationRepository;
import mk.ukim.finki.wp.kol2024g1.service.HotelService;
import mk.ukim.finki.wp.kol2024g1.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static mk.ukim.finki.wp.kol2024g1.service.specification.FieldFilterSpecification.*;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final HotelService hotelService;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(HotelService hotelService, ReservationRepository reservationRepository) {
        this.hotelService = hotelService;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Reservation> listAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElseThrow(InvalidReservationIdException::new);
    }

    @Override
    public Reservation create(String guestName, LocalDate dateCreated, Integer daysOfStay, RoomType roomType, Long hotelId) {
        Hotel hotel = hotelService.findById(hotelId);
        return reservationRepository.save(new Reservation(guestName,dateCreated,daysOfStay,roomType,hotel));
    }

    @Override
    public Reservation update(Long id, String guestName, LocalDate dateCreated, Integer daysOfStay, RoomType roomType, Long hotelId) {
        Hotel hotel = hotelService.findById(hotelId);
        Reservation reservation = this.findById(id);
        reservation.setGuestName(guestName);
        reservation.setDateCreated(dateCreated);
        reservation.setDaysOfStay(daysOfStay);
        reservation.setHotel(hotel);
        reservation.setRoomType(roomType);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation delete(Long id) {
        Reservation reservation = this.findById(id);
        reservationRepository.delete(reservation);
        return reservation;
    }

    @Override
    public Reservation extendStay(Long id) {
        Reservation reservation = this.findById(id);
        reservation.setDaysOfStay(reservation.getDaysOfStay()+1);
        return reservationRepository.save(reservation);
    }

    @Override
    public Page<Reservation> findPage(String guestName, RoomType roomType, Long hotel, int pageNum, int pageSize) {
        Specification<Reservation> specification = Specification
                .where(filterContainsText(Reservation.class, "guestName", guestName))
                .and(filterEqualsV(Reservation.class, "roomType", roomType))
                .and(filterEquals(Reservation.class, "hotel.id", hotel));

        return this.reservationRepository.findAll(
                specification,
                PageRequest.of(pageNum, pageSize)
        );
    }
}
