package com.halaq.backend.user.controller;

import com.halaq.backend.user.converter.FavoriteConverter;
import com.halaq.backend.user.criteria.FavoriteCriteria;
import com.halaq.backend.user.dto.FavoriteDto;
import com.halaq.backend.user.entity.Favorite;
import com.halaq.backend.user.service.facade.FavoriteService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Favorite Management")
@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController extends AbstractController<Favorite, FavoriteDto, FavoriteCriteria, FavoriteService, FavoriteConverter> {

    public FavoriteController(FavoriteService service, FavoriteConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Add a barber to favorites")
    @PostMapping("/")
    public ResponseEntity<FavoriteDto> save(@RequestBody FavoriteDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Finds favorites by criteria (e.g., by clientId)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<FavoriteDto>> findByCriteria(@RequestBody FavoriteCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Remove a favorite by id")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

    @Operation(summary = "Finds favorites by client ID")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FavoriteDto>> findByClientId(@PathVariable Long clientId) throws Exception {
        FavoriteCriteria favoriteCriteria = new FavoriteCriteria();
        favoriteCriteria.setClientId(clientId);
        return super.findByCriteria(favoriteCriteria);
    }

    @Operation(summary = "Finds favorites by barber ID")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<FavoriteDto>> findByBarberId(@PathVariable Long barberId) throws Exception {
        FavoriteCriteria favoriteCriteria = new FavoriteCriteria();
        favoriteCriteria.setBarberId(barberId);
        return super.findByCriteria(favoriteCriteria);
    }
}