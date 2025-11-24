package com.halaq.backend.core.controller;

public class AbstractHistoryController { /*< H extends HistBusinessObject,  HistoryCriteria extends BaseCriteria> {
    protected SERV service;
    protected AbstractConverter<T, DTO, H> converter;
    @Autowired
    private MessageSource messageSource;


    public ResponseEntity<AuditEntityDto> findHistoryById(Long id) throws Exception {
        AuditEntityDto h = service.findHistoryById(id);
        return new ResponseEntity<>(h, HttpStatus.OK);
    }

    public ResponseEntity<PaginatedList> findHistoryPaginatedByCriteria(HistoryCriteria criteria) throws Exception {
        List<AuditEntityDto> list = service.findHistoryPaginatedByCriteria(criteria, criteria.getPage(), criteria.getMaxResults(), criteria.getSortOrder(), criteria.getSortField());
        PaginatedList paginatedList = new PaginatedList();
        paginatedList.setList(list);
        if (list != null && !list.isEmpty()) {
            int dateSize = service.getHistoryDataSize(criteria);
            paginatedList.setDataSize(dateSize);
        }
        return new ResponseEntity<PaginatedList>(paginatedList, HttpStatus.OK);
    }

    public ResponseEntity<InputStreamResource> exportHistory(@RequestBody HistoryCriteria criteria) throws Exception {
        if (criteria.getExportModel() == null)
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        criteria.setMaxResults(null);
        return null;//TODO correct this bug
    }

    public ResponseEntity<Integer> getHistoryDataSize(@RequestBody HistoryCriteria criteria) throws Exception {
        int count = service.getHistoryDataSize(criteria);
        return new ResponseEntity<Integer>(count, HttpStatus.OK);
    }

    public AbstractHistoryController(SERV service, CONV converter) {
        this.service = service;
        this.converter = converter;
    }
    */

}
