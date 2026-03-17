import {
    Controller,
    Get,
    Post,
    Body,
    Param,
    Patch,
    UseGuards,
    Request,
    Query,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { BookingsService } from './bookings.service';
import { CreateBookingDto, UpdateBookingDto } from './dto';

@Controller('bookings')
@UseGuards(JwtAuthGuard)
export class BookingsController {
    constructor(private bookingsService: BookingsService) { }

    @Post()
    create(@Request() req, @Body() dto: CreateBookingDto) {
        return this.bookingsService.create(req.user.userId, dto);
    }

    @Get('my')
    getMyBookings(@Request() req) {
        return this.bookingsService.findAllForUser(req.user.userId);
    }

    @Get('shop/:shopId')
    getShopBookings(@Param('shopId') shopId: string, @Request() req) {
        return this.bookingsService.findAllForShop(shopId, req.user.userId);
    }

    @Get(':id')
    getOne(@Param('id') id: string) {
        return this.bookingsService.findOne(id);
    }

    @Patch(':id/status')
    updateStatus(@Param('id') id: string, @Request() req, @Body() dto: UpdateBookingDto) {
        return this.bookingsService.updateStatus(id, req.user.userId, dto);
    }
}
